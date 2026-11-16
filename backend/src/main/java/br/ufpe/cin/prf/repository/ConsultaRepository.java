package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {

    List<Consulta> findAllByOrderByDataAgendadaAsc();

    List<Consulta> findByPacienteCpfOrderByDataAgendadaAsc(String cpfPaciente);

    /** Agenda por período (usa o índice ix_consulta_data). */
    List<Consulta> findByDataAgendadaBetweenOrderByDataAgendadaAsc(LocalDateTime inicio, LocalDateTime fim);

    /** "origina" é 1:1: uma avaliação só pode originar um retorno. */
    boolean existsByAvaliacaoOrigemId(Integer idAvaliacao);

    boolean existsByAvaliacaoOrigemIdAndIdNot(Integer idAvaliacao, Integer idConsulta);
}
