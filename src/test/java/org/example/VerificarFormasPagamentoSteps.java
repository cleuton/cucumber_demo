package org.example;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.example.model.*;

import java.util.*;

// Supondo a existência de classes de modelo e serviços
import org.example.model.Cliente;
import org.example.PedidoService;
import org.example.PagamentoService;
import org.example.config.Parametros;
import org.example.FormasPagamentoService;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class VerificarFormasPagamentoSteps {

    @Mock
    private Cliente clienteMock;

    @Mock
    private Parametros parametrosMock;

    @Mock
    private PedidoService pedidoServiceMock;

    @Mock
    private PagamentoService pagamentoServiceMock;

    @InjectMocks
    private FormasPagamentoService formasPagamentoService;

    private List<String> formasPagamentoDisponiveis;

    private double valorDoPedido;
    private double valorDaEntrada;

    @Before
    public void setUp() {
        // Inicializa os mocks e injeta nas instâncias anotadas com @InjectMocks
        MockitoAnnotations.openMocks(this);
        when(parametrosMock.getValorLimiteUltimoPagamento()).thenReturn(500.00);
        when(parametrosMock.getValorLimiteUltimoPedido()).thenReturn(1000.00);
    }

    //@Given("^um cliente com (\\d+) meses de cadastro, situação de crédito \"([^\"]*)\", valor do último pedido (\\d+) e último pagamento (\\d+)$")
    @Given("um cliente com {int} meses de cadastro, situação de crédito {string}, valor do último pedido {int} e último pagamento {int}")
    public void um_cliente_com_meses_de_cadastro_situacao_de_credito_valor_do_ultimo_pedido_e_ultimo_pagamento(int mesesCadastro, String situacaoCredito, int valorUltimoPedido, int valorUltimoPagamento) {
        when(clienteMock.getSituacaoCredito()).thenReturn(situacaoCredito);
        when(clienteMock.getDataCadastro()).thenReturn(this.calculatePreviousDate(new Date(), mesesCadastro));
        when(pedidoServiceMock.getPedidos(anyInt())).thenReturn(getPedidos(5, valorUltimoPedido));
        when(pagamentoServiceMock.getPagamentosCliente(anyInt())).thenReturn(getPagamentos(10, valorUltimoPagamento));
        formasPagamentoService = new FormasPagamentoService(pedidoServiceMock, pagamentoServiceMock, parametrosMock);
    }

    @Given("^um pedido com valor de entrada (\\d+) e valor total (\\d+)$")
    public void um_pedido_com_valor_de_entrada_e_valor_total(int valorEntrada, int valorTotal) {
        // Simulação de configuração do pedido no cliente ou serviço conforme necessário
        this.valorDaEntrada = valorEntrada;
        this.valorDoPedido = valorTotal;
    }

    @When("^verificar as formas de pagamento disponíveis$")
    public void verificar_as_formas_de_pagamento_disponiveis() {
        formasPagamentoDisponiveis = formasPagamentoService.verificarFormasPagamento(clienteMock, valorDaEntrada, valorDoPedido);
    }

    @Then("^as formas de pagamento disponíveis devem ser (.*)$")
    public void as_formas_de_pagamento_disponíveis_devem_ser(String resultado) {
        List<String> formasEsperadas = Arrays.asList(resultado.split("\\s*,\\s*"));
        assertEquals(formasEsperadas, formasPagamentoDisponiveis);
    }


    private Date calculatePreviousDate (Date originalDate, int months) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1 * months);
        Date result = cal.getTime();
        return result;
    }

    private List<Pedido> getPedidos(int quantidade, double valorUltimo) {
        List<Pedido> pedidos = new ArrayList<Pedido>();
        for (int x=0; x<(quantidade-1); x++) {
            Pedido pedido = new Pedido(x+100, 100.00, 0.0);
            pedidos.add(pedido);
        }
        Pedido pedido = new Pedido(quantidade+100, valorUltimo, 0.0);
        pedidos.add(pedido);
        return pedidos;
    }

    List<Pagamento> getPagamentos(int quantidade, double valorUltimo) {
        List<Pagamento> pagamentos = new ArrayList<Pagamento>();
        for (int x=0; x<(quantidade-1); x++) {
            Pagamento pagamento = new Pagamento(x+100, 100.0, true);
            pagamentos.add(pagamento);
        }
        Pagamento pagamento = new Pagamento(quantidade+100, valorUltimo, true);
        pagamentos.add(pagamento);
        return pagamentos;
    }
}
