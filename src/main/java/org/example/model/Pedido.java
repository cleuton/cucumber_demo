package org.example.model;

public class Pedido implements Comparable<Pedido>{

    public Integer numero;
    public Double valor;
    public Double valorEntrada;

    public Pedido(Integer numero, Double valor, Double valorEntrada) {
        this.numero = numero;
        this.valor = valor;
        this.valorEntrada = valorEntrada;
    }

    @Override
    public int compareTo(Pedido outroPedido) {
        return this.numero.compareTo(outroPedido.numero);
    }
}
