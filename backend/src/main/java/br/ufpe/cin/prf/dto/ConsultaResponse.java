package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.Consulta;
import br.ufpe.cin.prf.entity.TipoConsulta;

import java.time.LocalDateTime;

public record ConsultaResponse(
        Integer idConsulta,
        LocalDateTime dataAgendada,
        TipoConsulta tipo,
        String cpfPaciente,
        String nomePaciente,
        String cpfProfissional,
        String nomeProfissional,
        String cpfEstagiario,
        String nomeEstagiario,
        Integer idAvaliacaoOrigem
) {
    public static ConsultaResponse from(Consulta c) {
        return new ConsultaResponse(
                c.getId(), c.getDataAgendada(), c.getTipo(),
                AvaliacaoPrfResponse.cpf(c.getPaciente()), AvaliacaoPrfResponse.nome(c.getPaciente()),
                AvaliacaoPrfResponse.cpf(c.getProfissional()), AvaliacaoPrfResponse.nome(c.getProfissional()),
                AvaliacaoPrfResponse.cpf(c.getEstagiario()), AvaliacaoPrfResponse.nome(c.getEstagiario()),
                c.getAvaliacaoOrigem() == null ? null : c.getAvaliacaoOrigem().getId());
    }
}
