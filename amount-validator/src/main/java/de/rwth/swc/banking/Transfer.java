package de.rwth.swc.banking;

public class Transfer {

    public Integer amount;
    public String fromIban;
    public String toIban;

    public Transfer(){}

    public Transfer(Integer amount, String fromIban, String toIban) {
        this.amount = amount;
        this.fromIban = fromIban;
        this.toIban = toIban;
    }

}
