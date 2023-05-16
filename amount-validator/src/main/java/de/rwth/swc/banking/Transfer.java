package de.rwth.swc.banking;

public class Transfer {

    public Integer amount;
    public String iban;

    public Transfer(Integer amount, String iban) {
        this.amount = amount;
        this.iban = iban;
    }

}
