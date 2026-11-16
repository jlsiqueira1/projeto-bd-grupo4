package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.OpcaoNescRequest;
import br.ufpe.cin.prf.dto.OpcaoNescResponse;
import br.ufpe.cin.prf.entity.CategoriaNesc;
import br.ufpe.cin.prf.entity.OpcaoNesc;
import br.ufpe.cin.prf.exception.ConflitoException;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.repository.OpcaoNescRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OpcaoNescService {

    private final OpcaoNescRepository opcaoRepository;

    public OpcaoNescService(OpcaoNescRepository opcaoRepository) {
        this.opcaoRepository = opcaoRepository;
    }

    public List<OpcaoNescResponse> listar(CategoriaNesc categoria) {
        List<OpcaoNesc> lista = categoria == null
                ? opcaoRepository.findAllByOrderByCategoriaAscSubtipoAscTextoAsc()
                : opcaoRepository.findByCategoriaOrderBySubtipoAscTextoAsc(categoria);
        return lista.stream().map(OpcaoNescResponse::from).toList();
    }

    public OpcaoNescResponse buscar(Integer id) {
        return OpcaoNescResponse.from(encontrar(id));
    }

    @Transactional
    public OpcaoNescResponse criar(OpcaoNescRequest req) {
        String texto = req.texto().trim();
        if (opcaoRepository.existsByCategoriaAndSubtipoAndTexto(req.categoria(), req.subtipo(), texto)) {
            throw new ConflitoException("Essa opção NESC já está cadastrada.");
        }
        OpcaoNesc opcao = new OpcaoNesc();
        preencher(opcao, req);
        return OpcaoNescResponse.from(opcaoRepository.saveAndFlush(opcao));
    }

    @Transactional
    public OpcaoNescResponse atualizar(Integer id, OpcaoNescRequest req) {
        OpcaoNesc opcao = encontrar(id);
        String texto = req.texto().trim();
        if (opcaoRepository.existsByCategoriaAndSubtipoAndTextoAndIdNot(req.categoria(), req.subtipo(), texto, id)) {
            throw new ConflitoException("Já existe outra opção NESC com a mesma categoria, subtipo e texto.");
        }
        preencher(opcao, req);
        return OpcaoNescResponse.from(opcaoRepository.saveAndFlush(opcao));
    }

    /** Bloqueado (409) se a opção já classificou alguma suspeita (ON DELETE RESTRICT). */
    @Transactional
    public void excluir(Integer id) {
        OpcaoNesc opcao = encontrar(id);
        opcaoRepository.delete(opcao);
        opcaoRepository.flush();
    }

    private OpcaoNesc encontrar(Integer id) {
        return opcaoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Opção NESC", id));
    }

    private static void preencher(OpcaoNesc o, OpcaoNescRequest req) {
        o.setCategoria(req.categoria());
        o.setSubtipo(req.subtipo());
        o.setTexto(req.texto().trim());
    }
}
