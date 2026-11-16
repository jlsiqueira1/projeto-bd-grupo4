package br.ufpe.cin.prf.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converte exceções em respostas HTTP padronizadas (ErroResponse).
 *
 * Duas camadas de validação:
 *   1. Os services verificam as regras antes de ir ao banco, para dar mensagens claras.
 *   2. O banco é a última barreira (CHECK, FK, UNIQUE e gatilhos). Se algo passar
 *      pela camada 1, o erro do PostgreSQL é capturado aqui e devolvido como
 *      409/422 com o nome da restrição violada, em vez de um 500 genérico.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Pattern NOME_RESTRICAO = Pattern.compile("constraint \"([^\"]+)\"");
    /** Em "... violates foreign key constraint "fk_x" on table "medicamento"", pega "medicamento". */
    private static final Pattern TABELA_REFERENCIADORA =
            Pattern.compile("constraint \"[^\"]+\" on table \"([^\"]+)\"");

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    /** Falhas do Bean Validation (@NotBlank, @Pattern, @NotNull...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> detalhes.add(e.getField() + ": " + e.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> detalhes.add(e.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição.", detalhes);
    }

    /** JSON malformado, data em formato errado ou valor de enum inexistente. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException ex) {
        String causa = ex.getMostSpecificCause().getMessage();
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido (JSON malformado, data fora do formato AAAA-MM-DD "
                        + "ou valor não aceito em um campo de lista fixa).",
                causa == null ? List.of() : List.of(primeiraLinha(causa)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Parâmetro '" + ex.getName() + "' com valor inválido: " + ex.getValue(), List.of());
    }

    /**
     * Erros vindos do banco. TransactionSystemException aparece quando o erro só
     * surge no COMMIT (gatilhos DEFERRABLE INITIALLY DEFERRED).
     */
    @ExceptionHandler({DataAccessException.class, TransactionSystemException.class})
    public ResponseEntity<ErroResponse> erroBanco(Exception ex) {
        SQLException sql = encontrarSqlException(ex);
        if (sql == null) {
            log.error("Erro de acesso a dados sem SQLException associada", ex);
            return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados.", List.of());
        }

        String estado = sql.getSQLState() == null ? "" : sql.getSQLState();
        String mensagemBanco = limpar(sql.getMessage());
        String restricao = extrair(NOME_RESTRICAO, mensagemBanco);
        List<String> detalhes = new ArrayList<>();
        if (restricao != null) {
            detalhes.add("restrição: " + restricao);
        }
        detalhes.add("banco: " + mensagemBanco);

        return switch (estado) {
            case "23505" -> resposta(HttpStatus.CONFLICT,
                    "Já existe um registro com esses valores (chave duplicada).", detalhes);
            case "23503" -> {
                String tabela = extrair(TABELA_REFERENCIADORA, mensagemBanco);
                boolean exclusao = mensagemBanco.contains("still referenced")
                        || mensagemBanco.contains("update or delete");
                String msg = exclusao
                        ? "Não é possível excluir/alterar: o registro ainda é referenciado"
                          + (tabela != null ? " pela tabela " + tabela : "") + " (ON DELETE RESTRICT)."
                        : "Referência inválida: o registro apontado não existe.";
                yield resposta(HttpStatus.CONFLICT, msg, detalhes);
            }
            case "23514" -> resposta(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Os dados violam uma regra de integridade do banco.", detalhes);
            case "23502" -> resposta(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Campo obrigatório não informado.", detalhes);
            default -> {
                if (estado.startsWith("22")) {
                    yield resposta(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Valor inválido para o tipo/tamanho da coluna.", detalhes);
                }
                log.error("Erro de banco não mapeado (SQLState {})", estado, ex);
                yield resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados.", detalhes);
            }
        };
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroGenerico(Exception ex) {
        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", List.of());
    }

    // ------------------------------------------------------------------ util

    private static ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status)
                .body(ErroResponse.of(status.value(), status.getReasonPhrase(), mensagem, detalhes));
    }

    /** Percorre a cadeia de causas até achar a SQLException do driver. */
    private static SQLException encontrarSqlException(Throwable ex) {
        Throwable atual = ex;
        while (atual != null) {
            if (atual instanceof SQLException sql) {
                // BatchUpdateException guarda a causa real em getNextException()
                SQLException proxima = sql.getNextException();
                return proxima != null ? proxima : sql;
            }
            if (atual.getCause() == atual) {
                break;
            }
            atual = atual.getCause();
        }
        return null;
    }

    private static String limpar(String mensagem) {
        if (mensagem == null) {
            return "";
        }
        return mensagem.replaceFirst("^ERROR:\\s*", "").replaceAll("\\s+Detail:\\s*", " | Detalhe: ")
                .replaceAll("\\s+Where:.*", "").trim();
    }

    private static String extrair(Pattern padrao, String texto) {
        Matcher m = padrao.matcher(texto);
        return m.find() ? m.group(1) : null;
    }

    private static String primeiraLinha(String texto) {
        int quebra = texto.indexOf('\n');
        return quebra >= 0 ? texto.substring(0, quebra) : texto;
    }
}
