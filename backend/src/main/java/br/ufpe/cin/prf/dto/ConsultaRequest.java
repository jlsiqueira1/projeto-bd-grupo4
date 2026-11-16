package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.TipoConsulta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

/**
 * Regras (service + CHECK/FK no banco):
 *  - exatamente um responsável: cpfProfissional ou cpfEstagiario;
 *  - tipo RETORNO se, e somente se, idAvaliacaoOrigem for informado;
 *  - a avaliação de origem precisa ser do mesmo paciente (FK composta);
 *  - cada avaliação origina no máximo um retorno (UNIQUE).
 * dataAgendada no formato AAAA-MM-DDTHH:MM (ex.: 2026-11-10T09:30).
 */
public record ConsultaRequest(
        @NotNull
        LocalDateTime dataAgendada,

        @NotNull
        TipoConsulta tipo,

        @NotBlank @Pattern(regexp = Validacoes.CPF_REGEX, message = Validacoes.CPF_MSG)
        String cpfPaciente,

        @Pattern(regexp = Validacoes.CPF_REGEX, message = Validacoes.CPF_MSG)
        String cpfProfissional,

        @Pattern(regexp = Validacoes.CPF_REGEX, message = Validacoes.CPF_MSG)
        String cpfEstagiario,

        Integer idAvaliacaoOrigem
) {
}
