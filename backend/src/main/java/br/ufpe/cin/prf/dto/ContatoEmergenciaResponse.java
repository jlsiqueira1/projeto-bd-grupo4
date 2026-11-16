package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.ContatoEmergencia;

public record ContatoEmergenciaResponse(
        String cpfPaciente,
        String nome,
        String telefone,
        String relacao
) {
    public static ContatoEmergenciaResponse from(ContatoEmergencia c) {
        return new ContatoEmergenciaResponse(c.getId().getCpfPaciente(), c.getId().getNome(),
                c.getTelefone(), c.getRelacao());
    }
}
