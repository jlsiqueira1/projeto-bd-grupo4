package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.CategoriaNesc;
import br.ufpe.cin.prf.entity.SubtipoNesc;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpcaoNescRequest(
        @NotNull
        CategoriaNesc categoria,

        @NotNull
        SubtipoNesc subtipo,

        @NotBlank @Size(max = 255)
        String texto
) {
}
