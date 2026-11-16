package br.ufpe.cin.prf.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Subclasse paciente: PK = cpf, que também é FK para pessoa. */
@Entity
@Table(name = "paciente")
@DiscriminatorValue(Paciente.TIPO)
@PrimaryKeyJoinColumn(name = "cpf")
@Getter
@Setter
public class Paciente extends Pessoa {

    public static final String TIPO = "PACIENTE";

    @Override
    public String getTipo() {
        return TIPO;
    }
}
