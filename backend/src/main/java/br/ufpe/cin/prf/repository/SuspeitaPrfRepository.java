package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.SuspeitaPrf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuspeitaPrfRepository extends JpaRepository<SuspeitaPrf, Integer> {

    List<SuspeitaPrf> findAllByOrderByIdAsc();

    List<SuspeitaPrf> findByAvaliacaoIdOrderByIdAsc(Integer idAvaliacao);

    boolean existsByAvaliacaoId(Integer idAvaliacao);

    boolean existsByMedicamentoId(Integer idMedicamento);
}
