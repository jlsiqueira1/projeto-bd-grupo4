package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.ContatoEmergencia;
import br.ufpe.cin.prf.entity.ContatoEmergenciaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContatoEmergenciaRepository extends JpaRepository<ContatoEmergencia, ContatoEmergenciaId> {

    /** Contatos de um paciente (navega pela chave composta: id.cpfPaciente). */
    List<ContatoEmergencia> findByIdCpfPacienteOrderByIdNomeAsc(String cpfPaciente);
}
