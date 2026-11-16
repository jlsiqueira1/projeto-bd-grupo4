package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.Estagiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstagiarioRepository extends JpaRepository<Estagiario, String> {

    List<Estagiario> findAllByOrderByNomeCompletoAsc();
}
