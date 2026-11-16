package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, String> {

    List<Paciente> findAllByOrderByNomeCompletoAsc();
}
