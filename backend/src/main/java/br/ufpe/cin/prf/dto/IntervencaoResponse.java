package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.Intervencao;
import br.ufpe.cin.prf.entity.TipoIntervencao;

public record IntervencaoResponse(
        Integer idIntervencao,
        Integer idSuspeita,
        TipoIntervencao tipo,
        String descricao
) {
    public static IntervencaoResponse from(Intervencao i) {
        return new IntervencaoResponse(i.getId(), i.getSuspeita().getId(), i.getTipo(), i.getDescricao());
    }
}
