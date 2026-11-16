package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.PessoaResponse;
import br.ufpe.cin.prf.entity.Pessoa;
import br.ufpe.cin.prf.exception.ConflitoException;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Operações sobre a superclasse pessoa. O cadastro é sempre feito pela
 * subclasse (paciente, profissional ou estagiário), porque a especialização é
 * total: não existe pessoa sem subclasse.
 */
@Service
@Transactional(readOnly = true)
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    public PessoaService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    public List<PessoaResponse> listar() {
        return pessoaRepository.findAllByOrderByNomeCompletoAsc().stream().map(PessoaResponse::from).toList();
    }

    public PessoaResponse buscar(String cpf) {
        return PessoaResponse.from(encontrar(cpf));
    }

    /**
     * Corrige um CPF digitado errado. O ON UPDATE CASCADE das FKs propaga a
     * mudança para todas as tabelas que referenciam essa pessoa.
     */
    @Transactional
    public PessoaResponse atualizarCpf(String cpfAtual, String novoCpf) {
        encontrar(cpfAtual);
        if (cpfAtual.equals(novoCpf)) {
            return buscar(cpfAtual);
        }
        if (pessoaRepository.existsById(novoCpf)) {
            throw new ConflitoException("Já existe uma pessoa com o CPF " + novoCpf + ".");
        }
        pessoaRepository.atualizarCpf(cpfAtual, novoCpf);
        return PessoaResponse.from(encontrar(novoCpf));
    }

    private Pessoa encontrar(String cpf) {
        return pessoaRepository.findById(cpf)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Pessoa com CPF", cpf));
    }
}
