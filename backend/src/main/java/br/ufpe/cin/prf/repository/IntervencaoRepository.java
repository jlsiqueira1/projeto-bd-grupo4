package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.Intervencao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntervencaoRepository extends JpaRepository<Intervencao, Integer> {

    List<Intervencao> findAllByOrderByIdAsc();

    List<Intervencao> findBySuspeitaIdOrderByIdAsc(Integer idSuspeita);
}
