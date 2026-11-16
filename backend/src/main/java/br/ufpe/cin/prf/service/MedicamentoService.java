package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.MedicamentoRequest;
import br.ufpe.cin.prf.dto.MedicamentoResponse;
import br.ufpe.cin.prf.entity.Medicamento;
import br.ufpe.cin.prf.entity.Paciente;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.exception.RegraNegocioException;
import br.ufpe.cin.prf.repository.MedicamentoRepository;
import br.ufpe.cin.prf.repository.SuspeitaPrfRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final SuspeitaPrfRepository suspeitaRepository;
    private final ReferenciaService referencias;

    public MedicamentoService(MedicamentoRepository medicamentoRepository,
                              SuspeitaPrfRepository suspeitaRepository,
                              ReferenciaService referencias) {
        this.medicamentoRepository = medicamentoRepository;
        this.suspeitaRepository = suspeitaRepository;
        this.referencias = referencias;
    }

    /** Lista todos ou, com cpfPaciente, só os medicamentos daquele paciente. */
    public List<MedicamentoResponse> listar(String cpfPaciente) {
        String cpf = Textos.vazioParaNulo(cpfPaciente);
        List<Medicamento> lista = cpf == null
                ? medicamentoRepository.findAllByOrderByIdAsc()
                : medicamentoRepository.findByPacienteCpfOrderByNomeAsc(cpf);
        return lista.stream().map(MedicamentoResponse::from).toList();
    }

    public MedicamentoResponse buscar(Integer id) {
        return MedicamentoResponse.from(encontrar(id));
    }

    @Transactional
    public MedicamentoResponse criar(MedicamentoRequest req) {
        Medicamento medicamento = new Medicamento();
        medicamento.setPaciente(referencias.paciente(req.cpfPaciente()));
        preencher(medicamento, req);
        return MedicamentoResponse.from(medicamentoRepository.saveAndFlush(medicamento));
    }

    @Transactional
    public MedicamentoResponse atualizar(Integer id, MedicamentoRequest req) {
        Medicamento medicamento = encontrar(id);
        Paciente novoPaciente = referencias.paciente(req.cpfPaciente());

        // Trocar o dono de um medicamento já usado em suspeitas deixaria a
        // suspeita apontando para um medicamento de outro paciente. O gatilho
        // só roda em suspeita_prf, então a regra é conferida aqui.
        boolean trocouPaciente = !novoPaciente.getCpf().equals(medicamento.getPaciente().getCpf());
        if (trocouPaciente && suspeitaRepository.existsByMedicamentoId(id)) {
            throw new RegraNegocioException("Não é possível trocar o paciente do medicamento " + id
                    + ": ele já está vinculado a suspeitas PRF.");
        }

        medicamento.setPaciente(novoPaciente);
        preencher(medicamento, req);
        return MedicamentoResponse.from(medicamentoRepository.saveAndFlush(medicamento));
    }

    /** Bloqueado (409) se o medicamento for alvo de alguma suspeita. */
    @Transactional
    public void excluir(Integer id) {
        Medicamento medicamento = encontrar(id);
        medicamentoRepository.delete(medicamento);
        medicamentoRepository.flush();
    }

    private Medicamento encontrar(Integer id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Medicamento", id));
    }

    private static void preencher(Medicamento m, MedicamentoRequest req) {
        m.setNome(req.nome().trim());
        m.setDose(req.dose().trim());
        m.setForma(req.forma().trim());
        m.setVia(req.via().trim());
        m.setFrequencia(req.frequencia().trim());
    }
}
