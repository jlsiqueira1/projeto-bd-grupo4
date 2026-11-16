package br.ufpe.cin.prf.exception;

/**
 * Vira HTTP 422. Usada quando o corpo da requisição é bem formado, mas fere
 * uma regra do minimundo (ex.: avaliação de estagiário sem supervisor).
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
