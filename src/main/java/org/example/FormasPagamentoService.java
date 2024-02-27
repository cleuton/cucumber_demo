package org.example;

import org.example.config.Parametros;
import org.example.model.Cliente;
import org.example.model.Pagamento;
import org.example.model.Pedido;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class FormasPagamentoService {

    private final Parametros parametros;
    private final PedidoService pedidoService;
    private final PagamentoService pagamentoService;

    public FormasPagamentoService(PedidoService pedidoService, PagamentoService pagamentoService,
                                  Parametros parametros) {
        this.pedidoService = pedidoService;
        this.pagamentoService = pagamentoService;
        this.parametros = parametros;
    }

    public List<String> verificarFormasPagamento(Cliente cliente, Double valorEntrada, Double valorPedido) {

        if (cliente == null) {
            throw new IllegalArgumentException("verificarFormasPagamento: Cliente não pode ser null");
        }

        if (valorEntrada == null) {
            throw new IllegalArgumentException("verificarFormasPagamento: Valor entrada não pode ser null");
        }

        if (valorPedido == null) {
            throw new IllegalArgumentException("verificarFormasPagamento: Valor do pedido não pode ser null");
        }

        if (valorPedido < 0) {
            throw new IllegalArgumentException("verificarFormasPagamento: Valor do pedido inválido");
        }

        if (valorEntrada < 0 || valorEntrada > valorPedido) {
            throw new IllegalArgumentException("verificarFormasPagamento: Valor entrada inválido");
        }


        List<String> formasPagamento = new ArrayList<>();
        List<Pedido> pedidosCliente = pedidoService.getPedidos(cliente.getId());
        Collections.sort(pedidosCliente);
        List<Pagamento> pagamentos = pagamentoService.getPagamentosCliente(cliente.getId());
        Collections.sort(pagamentos);

        double valorUltimoPagamento = getUltimoPagamento(pagamentos);
        double valorUltimoPedido = getValorUltimoPedido(pedidosCliente);
        int tempoCliente = getTempoCliente(cliente.getDataCadastro());
        boolean valorEntradaMaiorIgual20porcento = valorEntrada >= valorPedido * 0.2;

        formasPagamento.add("Pagamento à vista");

        if (tempoCliente >= 6 && !"ruim".equals(cliente.getSituacaoCredito())) {
            if ("boa".equals(cliente.getSituacaoCredito())) {
                formasPagamento.add("Pagamento em 2 vezes com juros");
                if (tempoCliente >= 12) {
                    formasPagamento.add("Pagamento em 3 vezes sem juros");
                }
                if (valorUltimoPedido >= parametros.getValorLimiteUltimoPedido()
                    && valorUltimoPagamento >= parametros.getValorLimiteUltimoPagamento()) {
                    formasPagamento.add("Pagamento em 6 vezes sem juros");
                }
            } else if (valorEntradaMaiorIgual20porcento) {
                formasPagamento.add("Pagamento em 2 vezes com juros");
            }
        }

        return formasPagamento;
    }

    private double getValorUltimoPedido(List<Pedido> pedidosCliente) {
        double valor = 0.0;
        if (!pedidosCliente.isEmpty()) {
            valor = pedidosCliente.get(pedidosCliente.size()-1).valor;
        }
        return valor;
    }

    private int getTempoCliente(Date dataCadastro) {
        // Convertendo as datas para LocalDate (java.time)
        LocalDate localDateAnterior = dataCadastro.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate localDateAtual = (new Date()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        // Calculando a diferença entre as datas
        Period periodo = Period.between(localDateAnterior, localDateAtual);

        // Convertendo a diferença em meses
        return periodo.getYears() * 12 + periodo.getMonths();
    }

    private double getUltimoPagamento(List<Pagamento> pagamentos) {
        double valor = 0.0;
        ListIterator<Pagamento> iterator = pagamentos.listIterator(pagamentos.size());
        while (iterator.hasPrevious()) {
            Pagamento pagamento = iterator.previous();
            if (pagamento.liquidado) {
                valor = pagamento.valor;
                break;
            }
        }
        return valor;
    }
}
