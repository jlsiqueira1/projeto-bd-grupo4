package br.ufpe.cin.prf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Subclasse estagiario. */
@Entity
@Table(name = "estagiario")
@DiscriminatorValue(Estagiario.TIPO)
@PrimaryKeyJoinColumn(name = "cpf")
@Getter
@Setter
public class Estagiario extends Pessoa {

    public static final String TIPO = "ESTAGIARIO";

    @Column(name = "instituicao_ensino", nullable = false, length = 150)
    private String instituicaoEnsino;

    @Column(name = "curso", nullable = false, length = 100)
    private String curso;

    @Override
    public String getTipo() {
        return TIPO;
    }
}
