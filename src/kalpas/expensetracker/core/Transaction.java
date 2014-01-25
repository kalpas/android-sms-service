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
        CARD_CREDIT(true, "card credit"), CARD_DEBIT(true, "card debit"), CASH_CREDIT(false, "cash_credit"), CASH_DEBIT(
                false, "cash debit"), WITHDRAWAL(true, "withdrawal"), DEPOSIT(true, "deposit");

        private boolean cardOperation;
        private String  name;

        private TranType(boolean cardOperation, String name) {
            this.cardOperation = cardOperation;
            this.name = name;
        }

        public boolean isCardOperation() {
            return cardOperation;
        }

        public static TranType forName(String typeName) {
            for (TranType type : TranType.values()) {
                if (type.name.equals(typeName)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Illegal transaction type: " + typeName);
        }
    }

}
