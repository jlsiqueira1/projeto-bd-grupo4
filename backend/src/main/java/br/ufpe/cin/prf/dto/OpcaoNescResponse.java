package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.CategoriaNesc;
import br.ufpe.cin.prf.entity.OpcaoNesc;
import br.ufpe.cin.prf.entity.SubtipoNesc;

public record OpcaoNescResponse(
        Integer idOpcaoNesc,
        CategoriaNesc categoria,
        SubtipoNesc subtipo,
        String texto
) {
    public static OpcaoNescResponse from(OpcaoNesc o) {
        return new OpcaoNescResponse(o.getId(), o.getCategoria(), o.getSubtipo(), o.getTexto());
    }
}
