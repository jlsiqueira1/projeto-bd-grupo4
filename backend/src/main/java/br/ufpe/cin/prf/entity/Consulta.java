package br.ufpe.cin.prf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Consulta — especialização opção 8C: tabela única com o discriminador "tipo"
 * (PRIMEIRA ou RETORNO). Aqui o tipo é um atributo comum (enum), e não herança
 * JPA, porque as duas subclasses têm os mesmos atributos.
 *
 * avaliacaoOrigem (relação "origina", 1:1): no banco, a FK é composta
 * (id_avaliacao_origem, cpf_paciente) para garantir o mesmo paciente. No JPA
 * basta mapear a coluna do id; a regra do mesmo paciente é conferida também
 * no ConsultaService para devolver uma mensagem clara.
 */
@Entity
@Table(name = "consulta")
@Getter
@Setter
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Integer id;

    @Column(name = "data_agendada", nullable = false)
    private LocalDateTime dataAgendada;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoConsulta tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cpf_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpf_profissional")
    private ProfissionalSaude profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpf_estagiario")
    private Estagiario estagiario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_avaliacao_origem")
    private AvaliacaoPrf avaliacaoOrigem;
}
