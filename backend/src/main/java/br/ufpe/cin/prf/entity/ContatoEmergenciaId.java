package br.ufpe.cin.prf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Chave primária composta da entidade fraca contato_emergencia:
 * chave do dono (cpf_paciente) + chave parcial (nome).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContatoEmergenciaId implements Serializable {

    @Column(name = "cpf_paciente", length = 11, nullable = false)
    private String cpfPaciente;

    @Column(name = "nome", length = 150, nullable = false)
    private String nome;
}
