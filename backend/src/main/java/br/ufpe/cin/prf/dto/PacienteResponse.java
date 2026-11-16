package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.Genero;
import br.ufpe.cin.prf.entity.Paciente;

import java.time.LocalDate;

public record PacienteResponse(
        String cpf,
        String nomeCompleto,
        LocalDate dataNascimento,
        Genero genero,
        String telefone,
        String tipo
) {
    public static PacienteResponse from(Paciente p) {
        return new PacienteResponse(p.getCpf(), p.getNomeCompleto(), p.getDataNascimento(),
                p.getGenero(), p.getTelefone(), p.getTipo());
    }
}
