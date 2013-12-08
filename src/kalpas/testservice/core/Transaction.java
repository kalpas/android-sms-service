package kalpas.testservice.core;

import java.io.Serializable;

import org.joda.time.DateTime;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 392982765103974346L;

    public Transaction(DateTime date) {
        this.date = date.toString();
        this.id = date.getMillis();
    }

    public Double amount;

    public String subject;

    public String tags;

    public String recipient;

    public String date;

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

}
