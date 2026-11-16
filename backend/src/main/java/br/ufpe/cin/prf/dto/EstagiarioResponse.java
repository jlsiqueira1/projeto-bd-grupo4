package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.Estagiario;
import br.ufpe.cin.prf.entity.Genero;

import java.time.LocalDate;

public record EstagiarioResponse(
        String cpf,
        String nomeCompleto,
        LocalDate dataNascimento,
        Genero genero,
        String telefone,
        String tipo,
        String instituicaoEnsino,
        String curso
) {
    public static EstagiarioResponse from(Estagiario e) {
        return new EstagiarioResponse(e.getCpf(), e.getNomeCompleto(), e.getDataNascimento(),
                e.getGenero(), e.getTelefone(), e.getTipo(),
                e.getInstituicaoEnsino(), e.getCurso());
    }
}
