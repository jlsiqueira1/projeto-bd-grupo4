package br.ufpe.cin.prf.service;

/** Pequenos utilitários de texto usados pelos services. */
final class Textos {

    private Textos() {
    }

    /** "" ou só espaços vira null; o resto é devolvido sem espaços nas pontas. */
    static String vazioParaNulo(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        return limpo.isEmpty() ? null : limpo;
    }

    static boolean presente(String valor) {
        return vazioParaNulo(valor) != null;
    }
}
