package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {

    List<Medicamento> findAllByOrderByIdAsc();

    List<Medicamento> findByPacienteCpfOrderByNomeAsc(String cpfPaciente);
}
