package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.ConsultaRequest;
import br.ufpe.cin.prf.dto.ConsultaResponse;
import br.ufpe.cin.prf.entity.AvaliacaoPrf;
import br.ufpe.cin.prf.entity.Consulta;
import br.ufpe.cin.prf.entity.TipoConsulta;
import br.ufpe.cin.prf.exception.ConflitoException;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.exception.RegraNegocioException;
import br.ufpe.cin.prf.repository.ConsultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final ReferenciaService referencias;

    public ConsultaService(ConsultaRepository consultaRepository, ReferenciaService referencias) {
        this.consultaRepository = consultaRepository;
        this.referencias = referencias;
    }

    /**
     * Agenda. Filtros opcionais: cpfPaciente, ou um período [inicio, fim]
     * (datas inclusivas). Sem filtro, lista todas em ordem de data.
     */
    public List<ConsultaResponse> listar(String cpfPaciente, LocalDate inicio, LocalDate fim) {
        String cpf = Textos.vazioParaNulo(cpfPaciente);
        List<Consulta> lista;
        if (cpf != null) {
            lista = consultaRepository.findByPacienteCpfOrderByDataAgendadaAsc(cpf);
        } else if (inicio != null || fim != null) {
            LocalDate de = inicio != null ? inicio : LocalDate.of(1900, 1, 1);
            LocalDate ate = fim != null ? fim : LocalDate.of(9999, 12, 31);
            if (ate.isBefore(de)) {
                throw new RegraNegocioException("O fim do período deve ser igual ou posterior ao início.");
            }
            lista = consultaRepository.findByDataAgendadaBetweenOrderByDataAgendadaAsc(
                    de.atStartOfDay(), ate.atTime(LocalTime.MAX));
        } else {
            lista = consultaRepository.findAllByOrderByDataAgendadaAsc();
        }
        return lista.stream().map(ConsultaResponse::from).toList();
    }

    public ConsultaResponse buscar(Integer id) {
        return ConsultaResponse.from(encontrar(id));
    }

    @Transactional
    public ConsultaResponse criar(ConsultaRequest req) {
        Consulta consulta = new Consulta();
        preencher(consulta, req, null);
        return ConsultaResponse.from(consultaRepository.saveAndFlush(consulta));
    }

    @Transactional
    public ConsultaResponse atualizar(Integer id, ConsultaRequest req) {
        Consulta consulta = encontrar(id);
        preencher(consulta, req, id);
        return ConsultaResponse.from(consultaRepository.saveAndFlush(consulta));
    }

    @Transactional
    public void excluir(Integer id) {
        Consulta consulta = encontrar(id);
        consultaRepository.delete(consulta);
        consultaRepository.flush();
    }

    /**
     * Mesmas regras de ck_consulta_responsavel, ck_consulta_retorno_origem,
     * uq_consulta_origem e da FK composta fk_consulta_avaliacao_origem.
     *
     * @param idAtual id da consulta sendo editada (null na criação)
     */
    private void preencher(Consulta c, ConsultaRequest req, Integer idAtual) {
        String cpfProfissional = Textos.vazioParaNulo(req.cpfProfissional());
        String cpfEstagiario = Textos.vazioParaNulo(req.cpfEstagiario());

        if ((cpfProfissional == null) == (cpfEstagiario == null)) {
            throw new RegraNegocioException(
                    "Informe exatamente um responsável: cpfProfissional OU cpfEstagiario.");
        }

        boolean retorno = req.tipo() == TipoConsulta.RETORNO;
        Integer idOrigem = req.idAvaliacaoOrigem();
        if (retorno && idOrigem == null) {
            throw new RegraNegocioException("Consulta de RETORNO exige idAvaliacaoOrigem.");
        }
        if (!retorno && idOrigem != null) {
            throw new RegraNegocioException("Só consultas de RETORNO têm avaliação de origem.");
        }

        AvaliacaoPrf origem = null;
        if (idOrigem != null) {
            origem = referencias.avaliacao(idOrigem);
            if (!origem.getPaciente().getCpf().equals(req.cpfPaciente())) {
                throw new RegraNegocioException("A avaliação " + idOrigem + " é do paciente "
                        + origem.getPaciente().getCpf() + ", não de " + req.cpfPaciente() + ".");
            }
            boolean jaUsada = idAtual == null
                    ? consultaRepository.existsByAvaliacaoOrigemId(idOrigem)
                    : consultaRepository.existsByAvaliacaoOrigemIdAndIdNot(idOrigem, idAtual);
            if (jaUsada) {
                throw new ConflitoException("A avaliação " + idOrigem
                        + " já originou uma consulta de retorno (relação 1:1).");
            }
        }

        c.setDataAgendada(req.dataAgendada());
        c.setTipo(req.tipo());
        c.setPaciente(referencias.paciente(req.cpfPaciente()));
        c.setProfissional(cpfProfissional == null ? null : referencias.profissional(cpfProfissional));
        c.setEstagiario(cpfEstagiario == null ? null : referencias.estagiario(cpfEstagiario));
        c.setAvaliacaoOrigem(origem);
    }

    private Consulta encontrar(Integer id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Consulta", id));
    }
}
