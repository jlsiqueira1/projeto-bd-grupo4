package br.ufpe.cin.prf.exception;

import java.time.OffsetDateTime;
import java.util.List;

/** Formato padrão de todas as respostas de erro da API. */
public record ErroResponse(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        List<String> detalhes
) {
    public static ErroResponse of(int status, String erro, String mensagem, List<String> detalhes) {
        return new ErroResponse(OffsetDateTime.now(), status, erro, mensagem, detalhes);
    }
}
