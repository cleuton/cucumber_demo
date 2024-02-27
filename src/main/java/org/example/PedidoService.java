package org.example;

import org.example.model.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoService {
    List<Pedido> getPedidos(Integer id);
}
