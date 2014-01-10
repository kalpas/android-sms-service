package kalpas.expensetracker.core;

import java.io.Serializable;
import java.util.Date;

import org.joda.time.DateTime;

public class Transaction implements Serializable {

    private static final long serialVersionUID = -568839622047507456L;

    public Transaction(DateTime date) {
        this.date = date;
        this.id = date.getMillis() + new Date().getTime();
        // + Math.round(Math.random() * Long.MAX_VALUE)
    }

    public String   cardId = "default";

    public Double   amount;

    public String   description;

    public String   tags;

    public String   recipient;

    public DateTime date;

    public long     id;

    public TranType tranType;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Transaction other = (Transaction) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return amount + " " + (description == null ? recipient : description) + "\n";
    }

    public enum TranType {
        CARD_CREDIT(true), CARD_DEBIT(true), CASH_CREDIT(false), CASH_DEBIT(false), WITHDRAWAL(true), DEPOSIT(true);

        private boolean cardOperation;

        private TranType(boolean cardOperation) {
            this.cardOperation = cardOperation;
        }

        public boolean isCardOperation() {
            return cardOperation;
        }
    }

}
