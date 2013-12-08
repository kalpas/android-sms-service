package kalpas.testservice.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kalpas.sms.parse.PumbTransaction;
import android.content.Context;
import android.util.Log;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

public class Core {

    private static final String CORE_FILE_NAME  = "core.json";
    private static final String TAG             = "kalpas.testservice.core.Core";

    private List<String>        cardIds         = new ArrayList<String>();
    // private List<Card> cards = new ArrayList<Card>();
    // private Multimap<String, Transaction> transactions =
    // ArrayListMultimap.create();

    private Gson                gson            = new Gson();
    private CardDAO             cardDao         = new CardDAO();
    private TransactionsDAO     transactionsDao = new TransactionsDAO();

    public Core(Context context) {
        try {
            FileInputStream fis = context.openFileInput(CORE_FILE_NAME);
            String[] fromJson = gson.fromJson(new InputStreamReader(fis), String[].class);
            if (fromJson != null) {
                cardIds = Lists.newArrayList(fromJson);
            } else {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            initCardList(context);
        }

        initCards(context);

    }

    private void initCards(Context context) {
        Card card = null;
        Set<Transaction> trxSet = null;
        for (String id : cardIds) {
            card = cardDao.load(id, context);
            if (card == null) {
                card = new Card();
                card.id = id;
                cardDao.save(card, context);
            }

            trxSet = transactionsDao.load(id, context);
            if (trxSet == null) {
                trxSet = new HashSet<Transaction>();
                transactionsDao.save(trxSet, id, context);
            }
        }
    }

    private void initCardList(Context context) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(CORE_FILE_NAME, Context.MODE_PRIVATE);
            if (cardIds.isEmpty()) {
                cardIds.add("default");
            }
            fos.write(gson.toJson(cardIds.toArray(new String[cardIds.size()])).getBytes());
            fos.close();

        } catch (FileNotFoundException e1) {
            Log.e(TAG, "error saving file", e1);
        } catch (IOException e1) {
            Log.e(TAG, "error saving file", e1);
        }
    }

    public Transaction processTransaction(PumbTransaction pumbTran, String cardId, Context context) {
        Transaction tran = null;

        switch (pumbTran.type) {
        case BLOCKED:
        case DEBITED:
            tran = processBlocked(pumbTran, context, cardId);
            break;
        case CREDITED:
            tran = processDebited(pumbTran, context, cardId);
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

    public void updateTransactionDetails(Transaction transaction, String cardId, Context context) {
        Set<Transaction> transactions = transactionsDao.load(cardId, context);
        transactions.remove(transaction);
        transactions.add(transaction);
        transactionsDao.save(transactions, cardId, context);
    }

    public String getSummary(Context context) {
        String cardId = cardIds.get(0);// FIXME temporary

        Card card = cardDao.load(cardId, context);
        StringBuilder builder = new StringBuilder();
        builder.append("available: " + card.left + "\n");
        builder.append("spent: " + card.spent + "\n");

        Set<Transaction> list = transactionsDao.load(cardId, context);
        builder.append("\nTransactions\n");
        for (Transaction tran : list) {
            builder.append(tran.amount + " " + tran.recipient + "\n");
        }
        return builder.toString();
    }

    private Transaction processDebited(PumbTransaction pumbTran, Context context, String cardId) {
        Card card = cardDao.load(cardId, context);
        card.left = pumbTran.remainingAvailable;
        cardDao.save(card, context);

        double amount = pumbTran.amountInAccountCurrency != null ? pumbTran.amountInAccountCurrency : pumbTran.amount;
        Set<Transaction> transactions = transactionsDao.load(cardId, context);
        Transaction tran = new Transaction(pumbTran.date);
        tran.amount = amount;
        tran.recipient = pumbTran.recipient;
        transactions.add(tran);
        transactionsDao.save(transactions, cardId, context);
        return tran;
    }

    private Transaction processBlocked(PumbTransaction pumbTran, Context context, String cardId) {
        Card card = cardDao.load(cardId, context);
        card.left = pumbTran.remainingAvailable;
        double amount = pumbTran.amountInAccountCurrency != null ? pumbTran.amountInAccountCurrency : pumbTran.amount;
        card.spent += amount;
        cardDao.save(card, context);

        Set<Transaction> transactions = transactionsDao.load(cardId, context);
        Transaction tran = new Transaction(pumbTran.date);
        tran.amount = -amount;
        tran.recipient = pumbTran.recipient;
        transactions.add(tran);
        transactionsDao.save(transactions, cardId, context);
        return tran;
    }

    public void clearData(Context context) {
        for (String id : cardIds) {
            cardDao.delete(id, context);
            transactionsDao.delete(id, context);
        }
        cardIds = new ArrayList<String>();
        initCardList(context);
        initCards(context);
    }

}
