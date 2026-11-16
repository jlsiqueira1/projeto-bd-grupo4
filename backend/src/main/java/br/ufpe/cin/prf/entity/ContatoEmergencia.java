package br.ufpe.cin.prf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidade fraca. @MapsId diz ao Hibernate que a parte cpfPaciente da chave
 * é a mesma coluna da FK para paciente (não há coluna duplicada).
 */
@Entity
@Table(name = "contato_emergencia")
@Getter
@Setter
public class ContatoEmergencia {

    @EmbeddedId
    private ContatoEmergenciaId id;

    @MapsId("cpfPaciente")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpf_paciente")
    private Paciente paciente;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "relacao", nullable = false, length = 50)
    private String relacao;
}
