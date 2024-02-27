package org.example.model;

import java.util.Date;

public class Cliente {
    private Integer id;
    private String situacaoCredito;
    private Date dataCadastro;

    public Cliente(Integer id, String situacaoCredito, Date dataCadastro) {
        this.id = id;
        this.situacaoCredito = situacaoCredito;
        this.dataCadastro = dataCadastro;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getSituacaoCredito() {
        return situacaoCredito;
    }

    public void setSituacaoCredito(String situacaoCredito) {
        this.situacaoCredito = situacaoCredito;
    }
}
