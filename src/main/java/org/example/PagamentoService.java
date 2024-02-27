package org.example;

import org.example.model.Pagamento;

import java.util.List;

public interface PagamentoService {
    List<Pagamento> getPagamentosCliente(Object id);
}
