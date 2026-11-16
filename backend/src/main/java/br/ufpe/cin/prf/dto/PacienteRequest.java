package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.Genero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Corpo de POST/PUT de paciente.
 * cpf é obrigatório no POST e ignorado no PUT (para corrigir um CPF use
 * PATCH /api/pessoas/{cpf}/cpf).
 */
public record PacienteRequest(
        @Pattern(regexp = Validacoes.CPF_REGEX, message = Validacoes.CPF_MSG)
        String cpf,

        @NotBlank @Size(max = 150)
        String nomeCompleto,

        @NotNull @Past
        LocalDate dataNascimento,

        @NotNull
        Genero genero,

        @NotBlank @Size(max = 20)
        String telefone
) {
}
