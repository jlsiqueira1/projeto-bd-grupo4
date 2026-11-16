package br.ufpe.cin.prf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Superclasse da especialização de pessoa.
 *
 * Mapeamento da opção 8A (uma tabela para a superclasse e uma para cada
 * subclasse) = herança JOINED do JPA. A coluna "tipo" é o discriminador:
 * o Hibernate grava o valor de @DiscriminatorValue da subclasse ao inserir.
 * Nas tabelas das subclasses, a coluna tipo tem DEFAULT fixo no banco,
 * o que fecha a FK composta (cpf, tipo) -> pessoa.
 */
@Entity
@Table(name = "pessoa")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 12)
@Getter
@Setter
public abstract class Pessoa {

    @Id
    @Column(name = "cpf", length = 11, nullable = false)
    private String cpf;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 20)
    private Genero genero;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    /** Valor gravado na coluna tipo (PACIENTE, PROFISSIONAL ou ESTAGIARIO). */
    public abstract String getTipo();
}
