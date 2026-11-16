package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.AvaliacaoPrf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvaliacaoPrfRepository extends JpaRepository<AvaliacaoPrf, Integer> {

    List<AvaliacaoPrf> findAllByOrderByDataRealizacaoDescIdDesc();

    /** Histórico de avaliações de um paciente (usa o índice ix_avaliacao_paciente). */
    List<AvaliacaoPrf> findByPacienteCpfOrderByDataRealizacaoDescIdDesc(String cpfPaciente);
}
