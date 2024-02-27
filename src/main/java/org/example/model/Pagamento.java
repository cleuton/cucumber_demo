package org.example.model;

import java.util.Date;

public class Pagamento implements Comparable<Pagamento> {
    public Integer numero;
    public Date vencimento;
    public Date pagamento;
    public Double valor;
    public Boolean liquidado;

    public Pagamento(int i, double v, boolean b) {
        super();
        this.numero = i;
        this.valor = v;
        this.liquidado = b;
    }


    @Override
    public int compareTo(Pagamento outroPagamento) {
        return this.numero.compareTo(outroPagamento.numero);
    }
}
