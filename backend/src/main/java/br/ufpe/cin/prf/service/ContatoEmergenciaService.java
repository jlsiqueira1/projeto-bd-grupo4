package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.ContatoEmergenciaRequest;
import br.ufpe.cin.prf.dto.ContatoEmergenciaResponse;
import br.ufpe.cin.prf.entity.ContatoEmergencia;
import br.ufpe.cin.prf.entity.ContatoEmergenciaId;
import br.ufpe.cin.prf.entity.Paciente;
import br.ufpe.cin.prf.exception.ConflitoException;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.exception.RegraNegocioException;
import br.ufpe.cin.prf.repository.ContatoEmergenciaRepository;
import br.ufpe.cin.prf.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Contatos são uma entidade fraca: só existem dentro de um paciente, por isso
 * os endpoints ficam aninhados em /api/pacientes/{cpf}/contatos.
 */
@Service
@Transactional(readOnly = true)
public class ContatoEmergenciaService {

    private final ContatoEmergenciaRepository contatoRepository;
    private final PacienteRepository pacienteRepository;

    public ContatoEmergenciaService(ContatoEmergenciaRepository contatoRepository,
                                    PacienteRepository pacienteRepository) {
        this.contatoRepository = contatoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    public List<ContatoEmergenciaResponse> listar(String cpfPaciente) {
        encontrarPaciente(cpfPaciente);
        return contatoRepository.findByIdCpfPacienteOrderByIdNomeAsc(cpfPaciente).stream()
                .map(ContatoEmergenciaResponse::from).toList();
    }

    public ContatoEmergenciaResponse buscar(String cpfPaciente, String nome) {
        return ContatoEmergenciaResponse.from(encontrar(cpfPaciente, nome));
    }

    @Transactional
    public ContatoEmergenciaResponse criar(String cpfPaciente, ContatoEmergenciaRequest req) {
        Paciente paciente = encontrarPaciente(cpfPaciente);
        String nome = Textos.vazioParaNulo(req.nome());
        if (nome == null) {
            throw new RegraNegocioException("O nome do contato é obrigatório.");
        }
        ContatoEmergenciaId id = new ContatoEmergenciaId(cpfPaciente, nome);
        if (contatoRepository.existsById(id)) {
            throw new ConflitoException("O paciente " + cpfPaciente + " já tem um contato chamado '" + nome + "'.");
        }

        ContatoEmergencia contato = new ContatoEmergencia();
        contato.setId(id);
        contato.setPaciente(paciente);
        contato.setTelefone(req.telefone().trim());
        contato.setRelacao(req.relacao().trim());
        return ContatoEmergenciaResponse.from(contatoRepository.saveAndFlush(contato));
    }

    /** O nome (chave parcial) vem da URL; só telefone e relação são alterados. */
    @Transactional
    public ContatoEmergenciaResponse atualizar(String cpfPaciente, String nome, ContatoEmergenciaRequest req) {
        ContatoEmergencia contato = encontrar(cpfPaciente, nome);
        contato.setTelefone(req.telefone().trim());
        contato.setRelacao(req.relacao().trim());
        return ContatoEmergenciaResponse.from(contatoRepository.saveAndFlush(contato));
    }

    @Transactional
    public void excluir(String cpfPaciente, String nome) {
        ContatoEmergencia contato = encontrar(cpfPaciente, nome);
        contatoRepository.delete(contato);
        contatoRepository.flush();
    }

    private Paciente encontrarPaciente(String cpf) {
        return pacienteRepository.findById(cpf)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Paciente com CPF", cpf));
    }

    private ContatoEmergencia encontrar(String cpfPaciente, String nome) {
        encontrarPaciente(cpfPaciente);
        return contatoRepository.findById(new ContatoEmergenciaId(cpfPaciente, nome))
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Contato '" + nome + "' do paciente " + cpfPaciente + " não encontrado."));
    }
}
