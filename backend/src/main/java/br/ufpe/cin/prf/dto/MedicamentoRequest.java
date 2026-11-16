package br.ufpe.cin.prf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MedicamentoRequest(
        @NotBlank @Pattern(regexp = Validacoes.CPF_REGEX, message = Validacoes.CPF_MSG)
        String cpfPaciente,

        @NotBlank @Size(max = 150)
        String nome,

        @NotBlank @Size(max = 50)
        String dose,

        @NotBlank @Size(max = 50)
        String forma,

        @NotBlank @Size(max = 50)
        String via,

        @NotBlank @Size(max = 100)
        String frequencia
) {
}
