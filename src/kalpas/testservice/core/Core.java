package kalpas.testservice.core;

import kalpas.sms.parse.PumbTransaction;
import android.content.Context;

public class Core {

    private PersistentCard         defaultCard;

    private PersistentTransactions defaultTransactions;

    public void init(Context context) {
        Card card = new Card();
        card.id = "default";
        defaultCard = new PersistentCard(card);
        defaultTransactions = new PersistentTransactions(card.id);
        defaultCard.persist(context);
        defaultTransactions.persist(context);

    }

    public void addTransaction(PumbTransaction pumbTran, Context context) {
        defaultCard.load(context);
        Card card = defaultCard.getCard();
        card.left = pumbTran.remainingAvailable;
        double amount = pumbTran.amountInAccountCurrency != null ? pumbTran.amountInAccountCurrency
                : pumbTran.amount;
        card.spent += amount;
        defaultCard.persist(context);
        
        defaultTransactions.load(context);
        Transaction tran = new Transaction();
        tran.amount = amount;
        tran.recipient = pumbTran.recipient;
        defaultTransactions.getTransactions().add(tran);
        defaultTransactions.persist(context);

    }

}
