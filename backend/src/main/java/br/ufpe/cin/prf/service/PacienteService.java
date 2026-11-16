package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.PacienteRequest;
import br.ufpe.cin.prf.dto.PacienteResponse;
import br.ufpe.cin.prf.entity.Paciente;
import br.ufpe.cin.prf.exception.ConflitoException;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.exception.RegraNegocioException;
import br.ufpe.cin.prf.repository.PacienteRepository;
import br.ufpe.cin.prf.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PessoaRepository pessoaRepository;

    public PacienteService(PacienteRepository pacienteRepository, PessoaRepository pessoaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.pessoaRepository = pessoaRepository;
    }

    public List<PacienteResponse> listar() {
        return pacienteRepository.findAllByOrderByNomeCompletoAsc().stream().map(PacienteResponse::from).toList();
    }

    public PacienteResponse buscar(String cpf) {
        return PacienteResponse.from(encontrar(cpf));
    }

    /**
     * Insere em pessoa e em paciente na MESMA transação (herança JOINED do
     * Hibernate). O gatilho de especialização total só confere no COMMIT.
     */
    @Transactional
    public PacienteResponse criar(PacienteRequest req) {
        String cpf = Textos.vazioParaNulo(req.cpf());
        if (cpf == null) {
            throw new RegraNegocioException("O CPF é obrigatório no cadastro.");
        }
        pessoaRepository.findById(cpf).ifPresent(p -> {
            throw new ConflitoException("CPF " + cpf + " já cadastrado (tipo " + p.getTipo() + ").");
        });

        Paciente paciente = new Paciente();
        paciente.setCpf(cpf);
        preencher(paciente, req);
        return PacienteResponse.from(pacienteRepository.saveAndFlush(paciente));
    }

    @Transactional
    public PacienteResponse atualizar(String cpf, PacienteRequest req) {
        Paciente paciente = encontrar(cpf);
        preencher(paciente, req);
        return PacienteResponse.from(pacienteRepository.saveAndFlush(paciente));
    }

    /**
     * Remove pessoa + paciente (e os contatos, por ON DELETE CASCADE).
     * Se houver medicamentos, avaliações ou consultas, o banco bloqueia
     * (ON DELETE RESTRICT) e a API devolve 409.
     */
    @Transactional
    public void excluir(String cpf) {
        Paciente paciente = encontrar(cpf);
        pacienteRepository.delete(paciente);
        pacienteRepository.flush();
    }

    private Paciente encontrar(String cpf) {
        return pacienteRepository.findById(cpf)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Paciente com CPF", cpf));
    }

    private static void preencher(Paciente p, PacienteRequest req) {
        p.setNomeCompleto(req.nomeCompleto().trim());
        p.setDataNascimento(req.dataNascimento());
        p.setGenero(req.genero());
        p.setTelefone(req.telefone().trim());
    }
}
