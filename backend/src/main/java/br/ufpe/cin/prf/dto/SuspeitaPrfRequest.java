package br.ufpe.cin.prf.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * dataRegistro é opcional: se omitida, assume a data de hoje (como o DEFAULT
 * CURRENT_DATE da coluna). O medicamento precisa ser do mesmo paciente da
 * avaliação (gatilho tg_suspeita_medicamento_paciente).
 */
public record SuspeitaPrfRequest(
        LocalDate dataRegistro,

        @NotNull
        Integer idAvaliacao,

        @NotNull
        Integer idMedicamento,

        @NotNull
        Integer idOpcaoNesc
) {
}
