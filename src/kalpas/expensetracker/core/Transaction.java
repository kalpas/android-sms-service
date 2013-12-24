package kalpas.expensetracker.core;

import java.io.Serializable;
import java.util.Date;

import org.joda.time.DateTime;

import com.google.common.primitives.Longs;

public class Transaction implements Serializable, Comparable<Transaction> {

    private static final long serialVersionUID = 392982765103974346L;

    public Transaction(DateTime date) {
        this.date = date;
        this.id = date.getMillis() + new Date().getTime();
    }

    public String cardId = "default";

    public Double amount;

    public String description;

    public String tags;

    public String recipient;

    public DateTime date;

    public long   id;

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

    @Override
    public int compareTo(Transaction another) {
        return Longs.compare(this.id, another.id);
    }

}
