package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.ProfissionalSaude;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, String> {

    List<ProfissionalSaude> findAllByOrderByNomeCompletoAsc();

    /** Chave alternativa (conselho, numero_registro). */
    boolean existsByConselhoAndNumeroRegistro(String conselho, String numeroRegistro);

    boolean existsByConselhoAndNumeroRegistroAndCpfNot(String conselho, String numeroRegistro, String cpf);
}
