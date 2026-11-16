package br.ufpe.cin.prf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Regras (conferidas no service e garantidas por CHECK no banco):
 *  - exatamente um entre cpfProfissional e cpfEstagiario;
 *  - cpfSupervisor obrigatório se, e somente se, houver cpfEstagiario;
 *  - dataRetorno, se informada, posterior a dataRealizacao.
 */
public record AvaliacaoPrfRequest(
        @NotNull
        LocalDate dataRealizacao,

        LocalDate dataRetorno,

        @NotBlank @Pattern(regexp = Validacoes.CPF_REGEX, message = Validacoes.CPF_MSG)
        String cpfPaciente,

        @Pattern(regexp = Validacoes.CPF_REGEX, message = Validacoes.CPF_MSG)
        String cpfProfissional,

        @Pattern(regexp = Validacoes.CPF_REGEX, message = Validacoes.CPF_MSG)
        String cpfEstagiario,

        @Pattern(regexp = Validacoes.CPF_REGEX, message = Validacoes.CPF_MSG)
        String cpfSupervisor
) {
}
