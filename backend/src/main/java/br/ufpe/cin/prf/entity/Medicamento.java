package br.ufpe.cin.prf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Relacionamento "usa" (1:N): a FK cpf_paciente fica no lado N e é obrigatória. */
@Entity
@Table(name = "medicamento")
@Getter
@Setter
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medicamento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpf_paciente", nullable = false)
    private Paciente paciente;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "dose", nullable = false, length = 50)
    private String dose;

    @Column(name = "forma", nullable = false, length = 50)
    private String forma;

    @Column(name = "via", nullable = false, length = 50)
    private String via;

    @Column(name = "frequencia", nullable = false, length = 100)
    private String frequencia;
}
