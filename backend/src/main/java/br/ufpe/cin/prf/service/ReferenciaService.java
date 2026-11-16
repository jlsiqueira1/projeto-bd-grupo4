package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.entity.AvaliacaoPrf;
import br.ufpe.cin.prf.entity.Estagiario;
import br.ufpe.cin.prf.entity.Medicamento;
import br.ufpe.cin.prf.entity.OpcaoNesc;
import br.ufpe.cin.prf.entity.Paciente;
import br.ufpe.cin.prf.entity.ProfissionalSaude;
import br.ufpe.cin.prf.entity.SuspeitaPrf;
import br.ufpe.cin.prf.exception.RegraNegocioException;
import br.ufpe.cin.prf.repository.AvaliacaoPrfRepository;
import br.ufpe.cin.prf.repository.EstagiarioRepository;
import br.ufpe.cin.prf.repository.MedicamentoRepository;
import br.ufpe.cin.prf.repository.OpcaoNescRepository;
import br.ufpe.cin.prf.repository.PacienteRepository;
import br.ufpe.cin.prf.repository.ProfissionalSaudeRepository;
import br.ufpe.cin.prf.repository.SuspeitaPrfRepository;
import org.springframework.stereotype.Service;

/**
 * Resolve as chaves estrangeiras que chegam no corpo das requisições.
 *
 * Se o registro apontado não existe, devolve 422 com uma mensagem clara
 * (o 404 fica reservado para o recurso da própria URL). Buscar pela subclasse
 * certa também garante o papel: um CPF de paciente informado como
 * cpfProfissional não é encontrado em profissional_saude.
 */
@Service
public class ReferenciaService {

    private final PacienteRepository pacienteRepository;
    private final ProfissionalSaudeRepository profissionalRepository;
    private final EstagiarioRepository estagiarioRepository;
    private final AvaliacaoPrfRepository avaliacaoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final OpcaoNescRepository opcaoNescRepository;
    private final SuspeitaPrfRepository suspeitaRepository;

    public ReferenciaService(PacienteRepository pacienteRepository,
                             ProfissionalSaudeRepository profissionalRepository,
                             EstagiarioRepository estagiarioRepository,
                             AvaliacaoPrfRepository avaliacaoRepository,
                             MedicamentoRepository medicamentoRepository,
                             OpcaoNescRepository opcaoNescRepository,
                             SuspeitaPrfRepository suspeitaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.estagiarioRepository = estagiarioRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.opcaoNescRepository = opcaoNescRepository;
        this.suspeitaRepository = suspeitaRepository;
    }

    public Paciente paciente(String cpf) {
        return pacienteRepository.findById(cpf).orElseThrow(() ->
                new RegraNegocioException("Não existe paciente com CPF " + cpf + "."));
    }

    public ProfissionalSaude profissional(String cpf) {
        return profissionalRepository.findById(cpf).orElseThrow(() ->
                new RegraNegocioException("Não existe profissional de saúde com CPF " + cpf + "."));
    }

    public Estagiario estagiario(String cpf) {
        return estagiarioRepository.findById(cpf).orElseThrow(() ->
                new RegraNegocioException("Não existe estagiário com CPF " + cpf + "."));
    }

    public AvaliacaoPrf avaliacao(Integer id) {
        return avaliacaoRepository.findById(id).orElseThrow(() ->
                new RegraNegocioException("Não existe avaliação com id " + id + "."));
    }

    public Medicamento medicamento(Integer id) {
        return medicamentoRepository.findById(id).orElseThrow(() ->
                new RegraNegocioException("Não existe medicamento com id " + id + "."));
    }

    public OpcaoNesc opcaoNesc(Integer id) {
        return opcaoNescRepository.findById(id).orElseThrow(() ->
                new RegraNegocioException("Não existe opção NESC com id " + id + "."));
    }

    public SuspeitaPrf suspeita(Integer id) {
        return suspeitaRepository.findById(id).orElseThrow(() ->
                new RegraNegocioException("Não existe suspeita com id " + id + "."));
    }
}
