package kalpas.expensetracker.core;

import java.io.Serializable;
import java.util.Date;

import org.joda.time.DateTime;

public class Transaction implements Serializable {

    private static final long serialVersionUID = -6460816620130857576L;

    public Transaction(DateTime date) {
        this.date = date;
        this.id = date.getMillis() + new Date().getTime();
    }

    public long     payerId;

    public String   payer;

    public boolean  toAccount;

    public Double   amount;

    public String   description;

    public String   tags;

    public long     recipientId;

    public String   recipient;

    public DateTime date;

    public long     id;

    public TranType type;

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
        EXPENSE("expense"), INCOME("income"), TRANSFER("transfer");

        private String name;

        private TranType(String name) {
            this.name = name;
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
