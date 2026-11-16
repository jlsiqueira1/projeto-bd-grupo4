package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.AvaliacaoPrfRequest;
import br.ufpe.cin.prf.dto.AvaliacaoPrfResponse;
import br.ufpe.cin.prf.entity.AvaliacaoPrf;
import br.ufpe.cin.prf.entity.Paciente;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.exception.RegraNegocioException;
import br.ufpe.cin.prf.repository.AvaliacaoPrfRepository;
import br.ufpe.cin.prf.repository.ConsultaRepository;
import br.ufpe.cin.prf.repository.SuspeitaPrfRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AvaliacaoPrfService {

    private final AvaliacaoPrfRepository avaliacaoRepository;
    private final SuspeitaPrfRepository suspeitaRepository;
    private final ConsultaRepository consultaRepository;
    private final ReferenciaService referencias;

    public AvaliacaoPrfService(AvaliacaoPrfRepository avaliacaoRepository,
                               SuspeitaPrfRepository suspeitaRepository,
                               ConsultaRepository consultaRepository,
                               ReferenciaService referencias) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.suspeitaRepository = suspeitaRepository;
        this.consultaRepository = consultaRepository;
        this.referencias = referencias;
    }

    public List<AvaliacaoPrfResponse> listar(String cpfPaciente) {
        String cpf = Textos.vazioParaNulo(cpfPaciente);
        List<AvaliacaoPrf> lista = cpf == null
                ? avaliacaoRepository.findAllByOrderByDataRealizacaoDescIdDesc()
                : avaliacaoRepository.findByPacienteCpfOrderByDataRealizacaoDescIdDesc(cpf);
        return lista.stream().map(AvaliacaoPrfResponse::from).toList();
    }

    public AvaliacaoPrfResponse buscar(Integer id) {
        return AvaliacaoPrfResponse.from(encontrar(id));
    }

    @Transactional
    public AvaliacaoPrfResponse criar(AvaliacaoPrfRequest req) {
        validarRegras(req);
        AvaliacaoPrf avaliacao = new AvaliacaoPrf();
        preencher(avaliacao, req, referencias.paciente(req.cpfPaciente()));
        return AvaliacaoPrfResponse.from(avaliacaoRepository.saveAndFlush(avaliacao));
    }

    @Transactional
    public AvaliacaoPrfResponse atualizar(Integer id, AvaliacaoPrfRequest req) {
        AvaliacaoPrf avaliacao = encontrar(id);
        validarRegras(req);
        Paciente novoPaciente = referencias.paciente(req.cpfPaciente());

        // Trocar o paciente de uma avaliação que já tem suspeitas ou retorno
        // quebraria a consistência da cadeia clínica (medicamento de outro
        // paciente; retorno cujo paciente mudaria pelo ON UPDATE CASCADE).
        if (!novoPaciente.getCpf().equals(avaliacao.getPaciente().getCpf())) {
            if (suspeitaRepository.existsByAvaliacaoId(id)) {
                throw new RegraNegocioException("Não é possível trocar o paciente da avaliação " + id
                        + ": ela já tem suspeitas PRF registradas.");
            }
            if (consultaRepository.existsByAvaliacaoOrigemId(id)) {
                throw new RegraNegocioException("Não é possível trocar o paciente da avaliação " + id
                        + ": ela já originou uma consulta de retorno.");
            }
        }

        preencher(avaliacao, req, novoPaciente);
        return AvaliacaoPrfResponse.from(avaliacaoRepository.saveAndFlush(avaliacao));
    }

    /** Bloqueado (409) se houver suspeitas ou consulta de retorno ligadas à avaliação. */
    @Transactional
    public void excluir(Integer id) {
        AvaliacaoPrf avaliacao = encontrar(id);
        avaliacaoRepository.delete(avaliacao);
        avaliacaoRepository.flush();
    }

    /**
     * Mesmas regras dos CHECKs ck_avaliacao_executor, ck_avaliacao_supervisor e
     * ck_avaliacao_datas, conferidas antes para devolver uma mensagem clara.
     */
    private static void validarRegras(AvaliacaoPrfRequest req) {
        boolean temProfissional = Textos.presente(req.cpfProfissional());
        boolean temEstagiario = Textos.presente(req.cpfEstagiario());
        boolean temSupervisor = Textos.presente(req.cpfSupervisor());

        if (temProfissional == temEstagiario) {
            throw new RegraNegocioException(
                    "Informe exatamente um executor: cpfProfissional OU cpfEstagiario.");
        }
        if (temEstagiario && !temSupervisor) {
            throw new RegraNegocioException(
                    "Avaliação conduzida por estagiário exige um profissional supervisor (cpfSupervisor).");
        }
        if (temProfissional && temSupervisor) {
            throw new RegraNegocioException(
                    "Avaliação conduzida por profissional não tem supervisor: deixe cpfSupervisor vazio.");
        }
        if (req.dataRetorno() != null && !req.dataRetorno().isAfter(req.dataRealizacao())) {
            throw new RegraNegocioException("A data de retorno deve ser posterior à data de realização.");
        }
    }

    private void preencher(AvaliacaoPrf a, AvaliacaoPrfRequest req, Paciente paciente) {
        String cpfProfissional = Textos.vazioParaNulo(req.cpfProfissional());
        String cpfEstagiario = Textos.vazioParaNulo(req.cpfEstagiario());
        String cpfSupervisor = Textos.vazioParaNulo(req.cpfSupervisor());

        a.setDataRealizacao(req.dataRealizacao());
        a.setDataRetorno(req.dataRetorno());
        a.setPaciente(paciente);
        a.setProfissional(cpfProfissional == null ? null : referencias.profissional(cpfProfissional));
        a.setEstagiario(cpfEstagiario == null ? null : referencias.estagiario(cpfEstagiario));
        a.setSupervisor(cpfSupervisor == null ? null : referencias.profissional(cpfSupervisor));
    }

    private AvaliacaoPrf encontrar(Integer id) {
        return avaliacaoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Avaliação", id));
    }
}
