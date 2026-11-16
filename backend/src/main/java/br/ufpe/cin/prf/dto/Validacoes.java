package br.ufpe.cin.prf.dto;

/** Constantes de validação compartilhadas pelos DTOs. */
public final class Validacoes {

    /**
     * Mesmo formato do CHECK ck_pessoa_cpf: 11 dígitos, sem pontos ou traço.
     * A string vazia também passa (campo opcional deixado em branco no formulário);
     * os services tratam "" como ausente, e os campos obrigatórios têm @NotBlank.
     */
    public static final String CPF_REGEX = "(\\d{11})?";
    public static final String CPF_MSG = "deve conter exatamente 11 dígitos, sem pontos ou traço";

    private Validacoes() {
    }
}
