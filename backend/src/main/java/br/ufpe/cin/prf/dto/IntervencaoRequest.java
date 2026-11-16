package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.TipoIntervencao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IntervencaoRequest(
        @NotNull
        Integer idSuspeita,

        @NotNull
        TipoIntervencao tipo,

        @NotBlank
        String descricao
) {
}
