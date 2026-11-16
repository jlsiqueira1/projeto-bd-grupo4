package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.EstagiarioRequest;
import br.ufpe.cin.prf.dto.EstagiarioResponse;
import br.ufpe.cin.prf.entity.Estagiario;
import br.ufpe.cin.prf.exception.ConflitoException;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.exception.RegraNegocioException;
import br.ufpe.cin.prf.repository.EstagiarioRepository;
import br.ufpe.cin.prf.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EstagiarioService {

    private final EstagiarioRepository estagiarioRepository;
    private final PessoaRepository pessoaRepository;

    public EstagiarioService(EstagiarioRepository estagiarioRepository, PessoaRepository pessoaRepository) {
        this.estagiarioRepository = estagiarioRepository;
        this.pessoaRepository = pessoaRepository;
    }

    public List<EstagiarioResponse> listar() {
        return estagiarioRepository.findAllByOrderByNomeCompletoAsc().stream()
                .map(EstagiarioResponse::from).toList();
    }

    public EstagiarioResponse buscar(String cpf) {
        return EstagiarioResponse.from(encontrar(cpf));
    }

    @Transactional
    public EstagiarioResponse criar(EstagiarioRequest req) {
        String cpf = Textos.vazioParaNulo(req.cpf());
        if (cpf == null) {
            throw new RegraNegocioException("O CPF é obrigatório no cadastro.");
        }
        pessoaRepository.findById(cpf).ifPresent(p -> {
            throw new ConflitoException("CPF " + cpf + " já cadastrado (tipo " + p.getTipo() + ").");
        });

        Estagiario estagiario = new Estagiario();
        estagiario.setCpf(cpf);
        preencher(estagiario, req);
        return EstagiarioResponse.from(estagiarioRepository.saveAndFlush(estagiario));
    }

    @Transactional
    public EstagiarioResponse atualizar(String cpf, EstagiarioRequest req) {
        Estagiario estagiario = encontrar(cpf);
        preencher(estagiario, req);
        return EstagiarioResponse.from(estagiarioRepository.saveAndFlush(estagiario));
    }

    /** Bloqueado (409) se o estagiário conduziu avaliações ou é responsável por consultas. */
    @Transactional
    public void excluir(String cpf) {
        Estagiario estagiario = encontrar(cpf);
        estagiarioRepository.delete(estagiario);
        estagiarioRepository.flush();
    }

    private Estagiario encontrar(String cpf) {
        return estagiarioRepository.findById(cpf)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Estagiário com CPF", cpf));
    }

    private static void preencher(Estagiario e, EstagiarioRequest req) {
        e.setNomeCompleto(req.nomeCompleto().trim());
        e.setDataNascimento(req.dataNascimento());
        e.setGenero(req.genero());
        e.setTelefone(req.telefone().trim());
        e.setInstituicaoEnsino(req.instituicaoEnsino().trim());
        e.setCurso(req.curso().trim());
    }
}
