package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.CategoriaNesc;
import br.ufpe.cin.prf.entity.SubtipoNesc;
import br.ufpe.cin.prf.entity.SuspeitaPrf;

import java.time.LocalDate;

public record SuspeitaPrfResponse(
        Integer idSuspeita,
        LocalDate dataRegistro,
        Integer idAvaliacao,
        String cpfPaciente,
        Integer idMedicamento,
        String nomeMedicamento,
        Integer idOpcaoNesc,
        CategoriaNesc categoriaNesc,
        SubtipoNesc subtipoNesc,
        String textoNesc
) {
    public static SuspeitaPrfResponse from(SuspeitaPrf s) {
        return new SuspeitaPrfResponse(
                s.getId(), s.getDataRegistro(),
                s.getAvaliacao().getId(), s.getAvaliacao().getPaciente().getCpf(),
                s.getMedicamento().getId(), s.getMedicamento().getNome(),
                s.getOpcaoNesc().getId(), s.getOpcaoNesc().getCategoria(),
                s.getOpcaoNesc().getSubtipo(), s.getOpcaoNesc().getTexto());
    }
}
