package br.ufpe.cin.prf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * nome é a chave parcial: obrigatório no POST e ignorado no PUT
 * (no PUT o nome vem da URL e só telefone/relação mudam).
 */
public record ContatoEmergenciaRequest(
        @Size(max = 150)
        String nome,

        @NotBlank @Size(max = 20)
        String telefone,

        @NotBlank @Size(max = 50)
        String relacao
) {
}
