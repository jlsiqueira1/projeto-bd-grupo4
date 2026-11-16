package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.Genero;
import br.ufpe.cin.prf.entity.ProfissionalSaude;

import java.time.LocalDate;

public record ProfissionalSaudeResponse(
        String cpf,
        String nomeCompleto,
        LocalDate dataNascimento,
        Genero genero,
        String telefone,
        String tipo,
        String conselho,
        String numeroRegistro,
        String especialidade
) {
    public static ProfissionalSaudeResponse from(ProfissionalSaude p) {
        return new ProfissionalSaudeResponse(p.getCpf(), p.getNomeCompleto(), p.getDataNascimento(),
                p.getGenero(), p.getTelefone(), p.getTipo(),
                p.getConselho(), p.getNumeroRegistro(), p.getEspecialidade());
    }
}
