package br.ufpe.cin.prf.dto;

import br.ufpe.cin.prf.entity.Medicamento;

public record MedicamentoResponse(
        Integer idMedicamento,
        String cpfPaciente,
        String nomePaciente,
        String nome,
        String dose,
        String forma,
        String via,
        String frequencia
) {
    public static MedicamentoResponse from(Medicamento m) {
        return new MedicamentoResponse(m.getId(), m.getPaciente().getCpf(), m.getPaciente().getNomeCompleto(),
                m.getNome(), m.getDose(), m.getForma(), m.getVia(), m.getFrequencia());
    }
}
