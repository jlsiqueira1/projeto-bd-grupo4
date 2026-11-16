package br.ufpe.cin.prf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AtualizarCpfRequest(
        @NotBlank @Pattern(regexp = Validacoes.CPF_REGEX, message = Validacoes.CPF_MSG)
        String novoCpf
) {
}
