package br.ufpe.cin.prf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Subclasse profissional_saude. (conselho, numero_registro) é chave alternativa. */
@Entity
@Table(name = "profissional_saude")
@DiscriminatorValue(ProfissionalSaude.TIPO)
@PrimaryKeyJoinColumn(name = "cpf")
@Getter
@Setter
public class ProfissionalSaude extends Pessoa {

    public static final String TIPO = "PROFISSIONAL";

    @Column(name = "conselho", nullable = false, length = 20)
    private String conselho;

    @Column(name = "numero_registro", nullable = false, length = 20)
    private String numeroRegistro;

    @Column(name = "especialidade", nullable = false, length = 100)
    private String especialidade;

    @Override
    public String getTipo() {
        return TIPO;
    }
}
