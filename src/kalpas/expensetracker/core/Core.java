package kalpas.expensetracker.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kalpas.sms.parse.PumbTransaction;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Functions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multisets;
import com.google.common.collect.Ordering;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

public class Core {

    private static final String   DEFAULT_CARD    = "default";
    private static final String   TAG             = "kalpas.expensetracker.core.Core";

    private final CardDAO         cardDao         = new CardDAO();
    private final TransactionsDAO transactionsDao = new TransactionsDAO();
    private final Context         context;

    public Core(Context context) {
        this.context = context;
        initCards();
    }

    public void addTransaction(Transaction transaction) {
        Set<Transaction> transactions = transactionsDao.load(context);
        transactions.add(transaction);
        transactionsDao.save(transactions, context);
    }

    public void clearData() {
        cardDao.delete(DEFAULT_CARD, context);
        transactionsDao.deleteAll(context);
        initCards();
    }

    public String getAccountSummary() {
        Card card = cardDao.load(DEFAULT_CARD, context);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("available: %.2f%n", card.left));
        builder.append(String.format("spent: %.2f%n", card.spent));
        return builder.toString();
    }

    public String getCardId(PumbTransaction pumbTransaction) {
        return "default";// FIXME
    }

    public String getStats() {
        StringBuilder text = new StringBuilder();

        Set<Transaction> trxs = transactionsDao.load(context);

        text.append(getATMStats(trxs));// ATM stats
        text.append("_____________\n");

        text.append(getTagStats(trxs));// tags

        return text.toString();
    }

    @Deprecated
    public String getSummary(Context context) {
        Card card = cardDao.load(DEFAULT_CARD, context);
        StringBuilder builder = new StringBuilder();
        builder.append("available: " + card.left + "\n");
        builder.append("spent: " + card.spent + "\n");

        Set<Transaction> set = transactionsDao.load(context);
        builder.append("\nTransactions\n");
        for (Transaction tran : set) {
            builder.append(tran.amount + " ");
            if (tran.description != null) {
                builder.append(tran.description + "\n");
            } else {
                builder.append(tran.recipient + "\n");
            }
        }
        return builder.toString();
    }

    public List<Transaction> getTransactions() {
        Set<Transaction> set = transactionsDao.load(context);
        ArrayList<Transaction> txs = Lists.newArrayList(set);
        return txs;
    }

    public Transaction processTransaction(PumbTransaction pumbTran) {
        Transaction tran = null;

        // TODO rollback

        switch (pumbTran.type) {
        case BLOCKED:
        case DEBITED:
            tran = processBlocked(pumbTran);
            break;
        case CREDITED:
            tran = processDebited(pumbTran);
            break;
        default:
            Log.e(TAG, "no handler for transaction type " + pumbTran.type);
            break;
        }

        return tran;

    }

    public void removeTransaction(Transaction trx) {
        Set<Transaction> transactions = transactionsDao.load(context);
        transactions.remove(trx);
        transactionsDao.save(transactions, context);

    }

    public void updateTransactionDetails(Transaction transaction) {
        Set<Transaction> transactions = transactionsDao.load(context);
        transactions.remove(transaction);
        transactions.add(transaction);
        transactionsDao.save(transactions, context);
    }

    private String getATMStats(Collection<Transaction> trxs) {
        StringBuilder text = new StringBuilder();

        SortedMultiset<String> atms = TreeMultiset.create();
        for (Transaction tx : trxs) {
            if (!StringUtils.isEmpty(tx.recipient)) {
                atms.add(tx.recipient);
            }
        }

        Iterator<String> iterator = Multisets.copyHighestCountFirst(atms).elementSet().iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            text.append(element);
            text.append(" (" + atms.count(element) + ")\n");
        }

        return text.toString();
    }

    private String getTagStats(Collection<Transaction> trxs) {
        StringBuilder text = new StringBuilder();

        SortedMultiset<String> tags = TreeMultiset.create();
        Map<String, Double> amounts = new HashMap<String, Double>();
        Splitter splitter = Splitter.on(",").trimResults();

        String mainTag;
        Double grandTotal = 0.;
        for (Transaction tx : trxs) {
            if (tx.amount >= 0) {
                continue;// skip incoming transactions
            }

            if (!StringUtils.isEmpty(tx.tags)) {
                Iterable<String> tagList = splitter.split(tx.tags);
                Iterables.addAll(tags, tagList);
                mainTag = tagList.iterator().next();

            } else {
                mainTag = "no tag";
                tags.add(mainTag);
            }

            Double value = amounts.get(mainTag);
            grandTotal += Math.abs(tx.amount);
            if (value == null) {
                amounts.put(mainTag, Math.abs(tx.amount));
            } else {
                value += Math.abs(tx.amount);
                amounts.put(mainTag, value);
            }
        }

        Iterator<String> iterator = Multisets.copyHighestCountFirst(tags).elementSet().iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            text.append(element);
            text.append(" (" + tags.count(element) + ")\n");
        }

        text.append("\n");

        Ordering<String> valueComparator = Ordering.natural().reverse().onResultOf(Functions.forMap(amounts))
                .compound(Ordering.natural());
        Map<String, Double> sortedAmounts = ImmutableSortedMap.copyOf(amounts, valueComparator);

        Card card = cardDao.load(DEFAULT_CARD, context);
        text.append(String.format("total spent: %.2f%n" + "while actually spent %.2f%n", grandTotal, card.spent));
        for (Map.Entry<String, Double> entry : sortedAmounts.entrySet()) {
            text.append(String.format("%s: %.2f (%.2f%%)%n", entry.getKey(), entry.getValue(),
                    (entry.getValue() / grandTotal) * 100));
        }

        return text.toString();

    }

    private void initCards() {
        Card card = null;
        Set<Transaction> trxSet = null;
        card = cardDao.load(DEFAULT_CARD, context);
        if (card == null) {
            card = new Card();
            card.id = DEFAULT_CARD;
            cardDao.save(card, context);
        }

        trxSet = transactionsDao.load(context);
        if (trxSet == null) {
            trxSet = new HashSet<Transaction>();
            transactionsDao.save(trxSet, context);
        }
    }

    private Transaction processBlocked(PumbTransaction pumbTran) {
        Card card = cardDao.load(DEFAULT_CARD, context);
        card.left = pumbTran.remainingAvailable;
        double amount = pumbTran.amountInAccountCurrency != null ? pumbTran.amountInAccountCurrency : pumbTran.amount;
        card.spent += amount;
        cardDao.save(card, context);

        Set<Transaction> transactions = transactionsDao.load(context);
        Transaction tran = new Transaction(pumbTran.date);
        tran.amount = -amount;
        tran.recipient = pumbTran.recipient;
        transactions.add(tran);
        transactionsDao.save(transactions, context);
        return tran;
    }

    private Transaction processDebited(PumbTransaction pumbTran) {
        Card card = cardDao.load(DEFAULT_CARD, context);
        card.left = pumbTran.remainingAvailable;
        cardDao.save(card, context);

        double amount = pumbTran.amountInAccountCurrency != null ? pumbTran.amountInAccountCurrency : pumbTran.amount;
        Set<Transaction> transactions = transactionsDao.load(context);
        Transaction tran = new Transaction(pumbTran.date);
        tran.amount = amount;
        tran.recipient = pumbTran.recipient;
        transactions.add(tran);
        transactionsDao.save(transactions, context);
        return tran;
    }
}
