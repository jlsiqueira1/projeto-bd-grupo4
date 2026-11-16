package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.AvaliacaoPrf;
import br.ufpe.cin.prf.entity.Pessoa;

import java.time.LocalDate;

public record AvaliacaoPrfResponse(
        Integer idAvaliacao,
        LocalDate dataRealizacao,
        LocalDate dataRetorno,
        String cpfPaciente,
        String nomePaciente,
        String cpfProfissional,
        String nomeProfissional,
        String cpfEstagiario,
        String nomeEstagiario,
        String cpfSupervisor,
        String nomeSupervisor
) {
    public static AvaliacaoPrfResponse from(AvaliacaoPrf a) {
        return new AvaliacaoPrfResponse(
                a.getId(), a.getDataRealizacao(), a.getDataRetorno(),
                cpf(a.getPaciente()), nome(a.getPaciente()),
                cpf(a.getProfissional()), nome(a.getProfissional()),
                cpf(a.getEstagiario()), nome(a.getEstagiario()),
                cpf(a.getSupervisor()), nome(a.getSupervisor()));
    }

    static String cpf(Pessoa p) {
        return p == null ? null : p.getCpf();
    }

    static String nome(Pessoa p) {
        return p == null ? null : p.getNomeCompleto();
    }
}
