![](./thumb.jpg)

# Este documento é parte do curso de Engenharia de Testes de Software

![](./me.jpeg)

[**Cleuton Sampaio**](https://www.cleutonsampaio.com/#contact)

# Como usar Cucumber com JUnit 5 sem perder a cabeça.

Este projeto ensinará vocês a utilizarem o Cucumber para criar testes baseados em **BDD** Behavior-driven Development.

Antes de começar, devo dizer que a **plataforma Java** é algo insano e seu ecossistema é pior ainda. Componentes não se falam e ninguém atualiza as dependências.
Eu ia utilizar **JBehave** mas fazê-lo funcionar com **JUnit Jupiter** é um trabalho **INSANO** e **INGLÓRIO**. Então resolvi mudar para o **Cucumber**.

Mas o **Cucumber** não é muito melhor. Porém, como ele existe para várias linguagens, seus desenvolvedores são mais cuidadosos.

Tendo dito isso, vamos à demonstração. 

O repositório é: [**https://github.com/cleuton/cucumber_demo**](https://github.com/cleuton/cucumber_demo)

## A regra de negócio

Temos um serviço responsável por verificar quais formas de pagamento são permitidas para determinado cliente. Eis a regra de negócio em **pseudo-código**: 
```code 

1) Adicionar: 'Pagamento à vista'.
2) Se cliente.tempo_cliente >= 6 meses e cliente.situacao_credito != "ruim": 
3)      se cliente.situacao_credito = "boa" ou pedido.valor_entrada >= 20%:
4)          - Adicionar: 'Pagamento em 2 vezes com juros'
5)          Se cliente.situacao_credito == 'boa' e cliente.tempo_cliente >= 1 ano:
6)              - Adicionar: 'Pagamento em 3 vezes sem juros'
7)              se o cliente.valor_ultimo_pedido >= 1000 e cliente.valor_ultimo_pagamento >= 500:
8)                  - Adicionar: 'Pagamento em 6 vezes sem juros'
```

E aqui está a **tabela de decisão** correspondente: 

| Crédito | Tempo | Último pedido | Último pagamento | entrada | Vista | 2 x cj | 3 x sj | 6 x sj |
| ------- | ----- | ------------- | ---------------- | --- | --- | --- | --- | --- |
| qualquer | < 6 meses | qualquer | qualquer | qualquer | S | N | N | N |
| ruim | qualquer | qualquer | qualquer | qualquer | S | N | N | N |
| regular | 6 meses | qualquer | qualquer | <20% | S | N | N | N |
| regular | 6 meses | qualquer | qualquer | >=20% | S | S | N | N |
| boa | 6 meses | qualquer | qualquer | qualquer | S | S | N | N |
| boa | 1 ano | qualquer | qualquer | qualquer | S | S | S | N |
| boa | 1 ano | >= 1000 | >= 500 |  qualquer | S | S | S | S |

O serviço já está pronto: **FormasPagamentoService**, assim como todo o código de teste.

## Arquivo de Features

A primeira coisa é criar um **arquivo de features** utilizando a linguagem **Gherkin**. E não. Gherkin não é padronizada entre os vários frameworks de **BDD**. Cada um tem suas particularidades.

> Gherkin não é padronizada entre os vários frameworks de BDD

Vamos criar um arquivo de features simplificado e utilizar um recurso muito legal do **Cucumber** que é a **Data Table**: 
```gherkin

Feature: Verificar formas de pagamento disponíveis para o cliente

  Scenario Outline: Cliente elegível para múltiplas formas de pagamento
    Given um cliente com <tempo_cliente> meses de cadastro, situação de crédito "<situacao_credito>", valor do último pedido <valor_ultimo_pedido> e último pagamento <valor_ultimo_pagamento>
    And um pedido com valor de entrada <valor_entrada> e valor total <valor_pedido>
    When verificar as formas de pagamento disponíveis
    Then as formas de pagamento disponíveis devem ser <formas_pagamento>

    Examples:
      | tempo_cliente | situacao_credito | valor_ultimo_pedido | valor_ultimo_pagamento | valor_entrada | valor_pedido | formas_pagamento                                         |
      | 6             | boa              | 500                 | 250                    | 100           | 500          | Pagamento à vista, Pagamento em 2 vezes com juros    |
      | 12            | boa              | 1500                | 750                    | 300           | 1500         | Pagamento à vista, Pagamento em 2 vezes com juros, Pagamento em 3 vezes sem juros, Pagamento em 6 vezes sem juros |
      | 7             | regular          | 800                 | 400                    | 200           | 1000         | Pagamento à vista, Pagamento em 2 vezes com juros |

```

**Given** (Dado que...) e **And** (E...) são **tokens** que identificam uma pré-condição, ou seja, algo que existe antes da execução do teste. **When** (Quando...) é a execução do teste em si, e **Then** (Então...) é a avaliação dos resultados do teste.

Dá para customizar e colocar em **Português** mas dá muito trabalho e fica completamente incompatível com qualquer outro framework que use **Gherkin**.

Este cenário poderia ser traduzido assim: 
```code 

  Resumo do cenário: Cliente elegível para múltiplas formas de pagamento
    Dado um cliente com <tempo_cliente> meses de cadastro, situação de crédito "<situacao_credito>", valor do último pedido <valor_ultimo_pedido> e último pagamento <valor_ultimo_pagamento>
    E um pedido com valor de entrada <valor_entrada> e valor total <valor_pedido>
    Quando formos verificar as formas de pagamento disponíveis
    Então as formas de pagamento disponíveis devem ser <formas_pagamento>
```

E você pode notar que eu estou usando atributos da tabela (entre ```<``` e ```>```). A tabela está definida com várias colunas: 
```code 

| tempo_cliente | situacao_credito | valor_ultimo_pedido | valor_ultimo_pagamento | valor_entrada | valor_pedido | formas_pagamento                                         |
```

Elas são separadas por "|". Então ele montará os vários testes com cada linha da tabela, por exemplo: 
```code 

    Dado um cliente com 6 meses de cadastro, situação de crédito "boa", valor do último pedido 500 e último pagamento 250
    E um pedido com valor de entrada 100 e valor total 500
    Quando formos verificar as formas de pagamento disponíveis
    Então as formas de pagamento disponíveis devem ser Pagamento à vista, Pagamento em 2 vezes com juros
```

E ele verificará se o retorno foi realmente isso: Pagamento à vista, Pagamento em 2 vezes com juros. E assim vai para cada linha da tabela.

Nem todos os casos estão cobertos nessa **Feature** mas não é difícil criá-los. 

## Arquivo de Steps

O arquivo de **Steps** mapeia cada **Step** do arquivo de **Features** em um método **Java**: 
```java

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
```

O **Cucumber** não tem a anotação **@And** então utilizamos dois **@Given**. A cada execução de **Step** do arquivo de **Features** é invocado um desses métodos. A maneira como o **Cucumber** mapeia o texto da **Step** para o método é meio **Mandrake**: Você pode utilizar **expressão regular** ou formatadores **{int}** **{string}**.

O mais **Ninja** foi mapear a lista de strings do **@Then**: 
```code 

Then as formas de pagamento disponíveis devem ser <formas_pagamento>
...
@Then("^as formas de pagamento disponíveis devem ser (.*)$")
```

Na **Data table** do arquivo de **Features** eu tenho a coluna **formas_pagamento** que às vezes é um único *string* e às vezes é uma lista de *strings**. Isso fica meio complicado de mapear. A solução foi
utilizar uma expressão regular (.*) indicando que virá um caráter repetindo, e receber em um **String**: 
```java

    @Then("^as formas de pagamento disponíveis devem ser (.*)$")
    public void as_formas_de_pagamento_disponíveis_devem_ser(String resultado) {
        List<String> formasEsperadas = Arrays.asList(resultado.split("\\s*,\\s*"));
        ...
```
E depois de gerar um **array** posso transformar em **List** e comparar. 

## O código de Runner

Só o **JUnit** e o **Surefire** não conseguirão executar o arquivo de **Steps**. Você precisa criar uma classe **Runner** e colocá-la junto com seu arquivo de **Steps**: 
```java


package org.example;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("org.example")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME,value = "src/test/resources/org/example/verificar_formas_pagamento.feature")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME,value = "org.example")
@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,value = "false")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,value = "pretty, html:target/cucumber-report/cucumber.html")
public class RunCucumberTest {
}
```

É meio **boilerplate** mas é necessário. As propriedades que precisamos configurar:

- **@SelectClasspathResource("org.example")**: Nome do pacote de testes.
- **FEATURES_PROPERTY_NAME**: **Path** relativo do arquivo de **Feature**.
- **GLUE_PROPERTY_NAME**: Nome do pacote de testes.
- **EXECUTION_DRY_RUN_PROPERTY_NAME**: Se é para o **Cucumber** apenas validar os testes.
- **PLUGIN_PROPERTY_NAME**: Como e onde você quer a saída dos testes.

## A configuração no Maven

**Maven** é complicado. Não é o Maven em si, mas as dependências! Foi uma luta, mas aqui está o meu **pom.xml** com os pontos mais importantes:
```xml

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>FormasPagamentoService</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>FormasPagamentoService</name>
    <url>http://maven.apache.org</url>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <junit.version>5.10.2</junit.version>
        <cucumber.version>7.15.0</cucumber.version>

    </properties>

    <dependencies>
        <!-- Cucumber -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-junit-platform-engine</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <!-- https://mvnrepository.com/artifact/junit/junit -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- https://mvnrepository.com/artifact/org.junit.platform/junit-platform-suite-engine -->
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-suite-engine</artifactId>
            <version>1.10.2</version>
            <scope>test</scope>
        </dependency>



        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>3.12.4</version> <!-- Ou a versão mais recente -->
            <scope>test</scope>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.jacoco/jacoco-maven-plugin -->
        <dependency>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-surefire-plugin -->
        <dependency>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version> <!-- Use a versão mais recente -->
                <configuration>
                    <properties>
                        <configurationParameters>
                            cucumber.junit-platform.naming-strategy=long
                        </configurationParameters>
                    </properties>
                    <includes>
                        <include>**/*Test.java</include>
                        <include>**/*Tests.java</include>
                        <include>**/Test*.java</include>
                        <include>**/Test*.java</include>
                    </includes>
                    <testFailureIgnore>false</testFailureIgnore>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <id>prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>prepare-package</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>


</project>

```

O **JaCoCo** não é necessário, mas poderia ser configurado também. 

Para executar os testes basta: 
```shell

mvn clean test
```

## Conclusão

Clone o repositório, marque com **Star**, curta este post e compartilhe. Pode executar em sua máquina pois ele usa apenas **Mocks**. Estou usando **Java 21** mas você pode mudar isso no **pom.xml**.
