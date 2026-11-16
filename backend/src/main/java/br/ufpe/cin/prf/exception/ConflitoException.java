package br.ufpe.cin.prf.exception;

/** Vira HTTP 409. Usada para chave duplicada (CPF, registro profissional etc.). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
