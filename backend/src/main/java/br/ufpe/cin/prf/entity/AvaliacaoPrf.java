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

import java.time.LocalDate;

/**
 * Avaliação PRF. Relacionamentos (todos 1:N, FK no lado da avaliação):
 *   recebe       -> paciente            (obrigatório)
 *   conduz_prof  -> profissional_saude  (exatamente um entre profissional e estagiário)
 *   conduz_estag -> estagiario
 *   supervisiona -> profissional_saude  (presente se, e só se, houver estagiário)
 */
@Entity
@Table(name = "avaliacao_prf")
@Getter
@Setter
public class AvaliacaoPrf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avaliacao")
    private Integer id;

    @Column(name = "data_realizacao", nullable = false)
    private LocalDate dataRealizacao;

    @Column(name = "data_retorno")
    private LocalDate dataRetorno;

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
    @JoinColumn(name = "cpf_supervisor")
    private ProfissionalSaude supervisor;
}
