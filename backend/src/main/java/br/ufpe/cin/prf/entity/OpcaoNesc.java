package br.ufpe.cin.prf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Catálogo NESC (tabela de domínio). UNIQUE (categoria, subtipo, texto). */
@Entity
@Table(name = "opcao_nesc")
@Getter
@Setter
public class OpcaoNesc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcao_nesc")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 15)
    private CategoriaNesc categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "subtipo", nullable = false, length = 15)
    private SubtipoNesc subtipo;

    @Column(name = "texto", nullable = false, length = 255)
    private String texto;
}
