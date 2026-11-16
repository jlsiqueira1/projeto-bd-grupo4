package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.Genero;
import br.ufpe.cin.prf.entity.Pessoa;

import java.time.LocalDate;

/** Visão genérica de qualquer pessoa (superclasse), com o tipo da subclasse. */
public record PessoaResponse(
        String cpf,
        String nomeCompleto,
        LocalDate dataNascimento,
        Genero genero,
        String telefone,
        String tipo
) {
    public static PessoaResponse from(Pessoa p) {
        return new PessoaResponse(p.getCpf(), p.getNomeCompleto(), p.getDataNascimento(),
                p.getGenero(), p.getTelefone(), p.getTipo());
    }
}
