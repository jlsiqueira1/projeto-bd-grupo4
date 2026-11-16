package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.SuspeitaPrfRequest;
import br.ufpe.cin.prf.dto.SuspeitaPrfResponse;
import br.ufpe.cin.prf.entity.AvaliacaoPrf;
import br.ufpe.cin.prf.entity.Medicamento;
import br.ufpe.cin.prf.entity.SuspeitaPrf;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.exception.RegraNegocioException;
import br.ufpe.cin.prf.repository.SuspeitaPrfRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SuspeitaPrfService {

    private final SuspeitaPrfRepository suspeitaRepository;
    private final ReferenciaService referencias;

    public SuspeitaPrfService(SuspeitaPrfRepository suspeitaRepository, ReferenciaService referencias) {
        this.suspeitaRepository = suspeitaRepository;
        this.referencias = referencias;
    }

    public List<SuspeitaPrfResponse> listar(Integer idAvaliacao) {
        List<SuspeitaPrf> lista = idAvaliacao == null
                ? suspeitaRepository.findAllByOrderByIdAsc()
                : suspeitaRepository.findByAvaliacaoIdOrderByIdAsc(idAvaliacao);
        return lista.stream().map(SuspeitaPrfResponse::from).toList();
    }

    public SuspeitaPrfResponse buscar(Integer id) {
        return SuspeitaPrfResponse.from(encontrar(id));
    }

    @Transactional
    public SuspeitaPrfResponse criar(SuspeitaPrfRequest req) {
        SuspeitaPrf suspeita = new SuspeitaPrf();
        preencher(suspeita, req);
        return SuspeitaPrfResponse.from(suspeitaRepository.saveAndFlush(suspeita));
    }

    @Transactional
    public SuspeitaPrfResponse atualizar(Integer id, SuspeitaPrfRequest req) {
        SuspeitaPrf suspeita = encontrar(id);
        preencher(suspeita, req);
        return SuspeitaPrfResponse.from(suspeitaRepository.saveAndFlush(suspeita));
    }

    /** Bloqueado (409) se a suspeita já gerou intervenções. */
    @Transactional
    public void excluir(Integer id) {
        SuspeitaPrf suspeita = encontrar(id);
        suspeitaRepository.delete(suspeita);
        suspeitaRepository.flush();
    }

    private void preencher(SuspeitaPrf s, SuspeitaPrfRequest req) {
        AvaliacaoPrf avaliacao = referencias.avaliacao(req.idAvaliacao());
        Medicamento medicamento = referencias.medicamento(req.idMedicamento());

        // Mesma regra do gatilho tg_suspeita_medicamento_paciente.
        String pacienteAvaliacao = avaliacao.getPaciente().getCpf();
        String pacienteMedicamento = medicamento.getPaciente().getCpf();
        if (!pacienteAvaliacao.equals(pacienteMedicamento)) {
            throw new RegraNegocioException("O medicamento " + medicamento.getId()
                    + " pertence ao paciente " + pacienteMedicamento + ", mas a avaliação "
                    + avaliacao.getId() + " é do paciente " + pacienteAvaliacao + ".");
        }

        s.setAvaliacao(avaliacao);
        s.setMedicamento(medicamento);
        s.setOpcaoNesc(referencias.opcaoNesc(req.idOpcaoNesc()));
        s.setDataRegistro(req.dataRegistro() != null ? req.dataRegistro() : LocalDate.now());
    }

    private SuspeitaPrf encontrar(Integer id) {
        return suspeitaRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Suspeita", id));
    }
}
