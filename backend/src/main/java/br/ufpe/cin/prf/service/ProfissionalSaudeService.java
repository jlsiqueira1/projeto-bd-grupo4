package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.ProfissionalSaudeRequest;
import br.ufpe.cin.prf.dto.ProfissionalSaudeResponse;
import br.ufpe.cin.prf.entity.ProfissionalSaude;
import br.ufpe.cin.prf.exception.ConflitoException;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.exception.RegraNegocioException;
import br.ufpe.cin.prf.repository.PessoaRepository;
import br.ufpe.cin.prf.repository.ProfissionalSaudeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProfissionalSaudeService {

    private final ProfissionalSaudeRepository profissionalRepository;
    private final PessoaRepository pessoaRepository;

    public ProfissionalSaudeService(ProfissionalSaudeRepository profissionalRepository,
                                    PessoaRepository pessoaRepository) {
        this.profissionalRepository = profissionalRepository;
        this.pessoaRepository = pessoaRepository;
    }

    public List<ProfissionalSaudeResponse> listar() {
        return profissionalRepository.findAllByOrderByNomeCompletoAsc().stream()
                .map(ProfissionalSaudeResponse::from).toList();
    }

    public ProfissionalSaudeResponse buscar(String cpf) {
        return ProfissionalSaudeResponse.from(encontrar(cpf));
    }

    @Transactional
    public ProfissionalSaudeResponse criar(ProfissionalSaudeRequest req) {
        String cpf = Textos.vazioParaNulo(req.cpf());
        if (cpf == null) {
            throw new RegraNegocioException("O CPF é obrigatório no cadastro.");
        }
        pessoaRepository.findById(cpf).ifPresent(p -> {
            throw new ConflitoException("CPF " + cpf + " já cadastrado (tipo " + p.getTipo() + ").");
        });
        String conselho = req.conselho().trim();
        String registro = req.numeroRegistro().trim();
        if (profissionalRepository.existsByConselhoAndNumeroRegistro(conselho, registro)) {
            throw new ConflitoException("Já existe profissional com registro " + registro
                    + " no conselho " + conselho + ".");
        }

        ProfissionalSaude profissional = new ProfissionalSaude();
        profissional.setCpf(cpf);
        preencher(profissional, req);
        return ProfissionalSaudeResponse.from(profissionalRepository.saveAndFlush(profissional));
    }

    @Transactional
    public ProfissionalSaudeResponse atualizar(String cpf, ProfissionalSaudeRequest req) {
        ProfissionalSaude profissional = encontrar(cpf);
        String conselho = req.conselho().trim();
        String registro = req.numeroRegistro().trim();
        if (profissionalRepository.existsByConselhoAndNumeroRegistroAndCpfNot(conselho, registro, cpf)) {
            throw new ConflitoException("Já existe outro profissional com registro " + registro
                    + " no conselho " + conselho + ".");
        }
        preencher(profissional, req);
        return ProfissionalSaudeResponse.from(profissionalRepository.saveAndFlush(profissional));
    }

    /** Bloqueado (409) se o profissional conduziu, supervisionou ou é responsável por consultas. */
    @Transactional
    public void excluir(String cpf) {
        ProfissionalSaude profissional = encontrar(cpf);
        profissionalRepository.delete(profissional);
        profissionalRepository.flush();
    }

    private ProfissionalSaude encontrar(String cpf) {
        return profissionalRepository.findById(cpf)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Profissional de saúde com CPF", cpf));
    }

    private static void preencher(ProfissionalSaude p, ProfissionalSaudeRequest req) {
        p.setNomeCompleto(req.nomeCompleto().trim());
        p.setDataNascimento(req.dataNascimento());
        p.setGenero(req.genero());
        p.setTelefone(req.telefone().trim());
        p.setConselho(req.conselho().trim());
        p.setNumeroRegistro(req.numeroRegistro().trim());
        p.setEspecialidade(req.especialidade().trim());
    }
}
