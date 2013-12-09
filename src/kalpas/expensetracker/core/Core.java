package kalpas.expensetracker.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import kalpas.sms.parse.PumbTransaction;
import android.content.Context;
import android.util.Log;

public class Core {

    private static final String DEFAULT_CARD    = "default";
    private static final String TAG             = "kalpas.expensetracker.core.Core";

    private CardDAO             cardDao         = new CardDAO();
    private TransactionsDAO     transactionsDao = new TransactionsDAO();

    public Core(Context context) {
        initCards(context);
    }

    private void initCards(Context context) {
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

    public Transaction processTransaction(PumbTransaction pumbTran, Context context) {
        Transaction tran = null;

        switch (pumbTran.type) {
        case BLOCKED:
        case DEBITED:
            tran = processBlocked(pumbTran, context);
            break;
        case CREDITED:
            tran = processDebited(pumbTran, context);
            break;
        default:
            Log.e(TAG, "no handler for transaction type " + pumbTran.type);
            break;
        }

        return tran;

    }

    public String getCardId(PumbTransaction pumbTransaction) {
        return "default";// FIXME
    }

    public void updateTransactionDetails(Transaction transaction, Context context) {
        Set<Transaction> transactions = transactionsDao.load(context);
        transactions.remove(transaction);
        transactions.add(transaction);
        transactionsDao.save(transactions, context);
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
            if (tran.subject != null) {
                builder.append(tran.subject + "\n");
            } else {
                builder.append(tran.recipient + "\n");
            }
        }
        return builder.toString();
    }

    public List<Transaction> getTransactions(Context context) {
        Set<Transaction> set = transactionsDao.load(context);
        ArrayList<Transaction> txs = Lists.newArrayList(set);
        Collections.sort(txs);
        return txs;
    }


    public String getAccountSummary(Context context) {
        Card card = cardDao.load(DEFAULT_CARD, context);
        StringBuilder builder = new StringBuilder();
        builder.append("available: " + card.left + "\n");
        builder.append("spent: " + card.spent + "\n");
        return builder.toString();
    }

    private Transaction processDebited(PumbTransaction pumbTran, Context context) {
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

    private Transaction processBlocked(PumbTransaction pumbTran, Context context) {
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

    public void clearData(Context context) {
        cardDao.delete(DEFAULT_CARD, context);
        transactionsDao.deleteAll(context);
        initCards(context);
    }

}
