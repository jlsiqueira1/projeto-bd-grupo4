package br.ufpe.cin.prf.service;

import br.ufpe.cin.prf.dto.IntervencaoRequest;
import br.ufpe.cin.prf.dto.IntervencaoResponse;
import br.ufpe.cin.prf.entity.Intervencao;
import br.ufpe.cin.prf.exception.RecursoNaoEncontradoException;
import br.ufpe.cin.prf.repository.IntervencaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class IntervencaoService {

    private final IntervencaoRepository intervencaoRepository;
    private final ReferenciaService referencias;

    public IntervencaoService(IntervencaoRepository intervencaoRepository, ReferenciaService referencias) {
        this.intervencaoRepository = intervencaoRepository;
        this.referencias = referencias;
    }

    public List<IntervencaoResponse> listar(Integer idSuspeita) {
        List<Intervencao> lista = idSuspeita == null
                ? intervencaoRepository.findAllByOrderByIdAsc()
                : intervencaoRepository.findBySuspeitaIdOrderByIdAsc(idSuspeita);
        return lista.stream().map(IntervencaoResponse::from).toList();
    }

    public IntervencaoResponse buscar(Integer id) {
        return IntervencaoResponse.from(encontrar(id));
    }

    @Transactional
    public IntervencaoResponse criar(IntervencaoRequest req) {
        Intervencao intervencao = new Intervencao();
        preencher(intervencao, req);
        return IntervencaoResponse.from(intervencaoRepository.saveAndFlush(intervencao));
    }

    @Transactional
    public IntervencaoResponse atualizar(Integer id, IntervencaoRequest req) {
        Intervencao intervencao = encontrar(id);
        preencher(intervencao, req);
        return IntervencaoResponse.from(intervencaoRepository.saveAndFlush(intervencao));
    }

    @Transactional
    public void excluir(Integer id) {
        Intervencao intervencao = encontrar(id);
        intervencaoRepository.delete(intervencao);
        intervencaoRepository.flush();
    }

    private void preencher(Intervencao i, IntervencaoRequest req) {
        i.setSuspeita(referencias.suspeita(req.idSuspeita()));
        i.setTipo(req.tipo());
        i.setDescricao(req.descricao().trim());
    }

    private Intervencao encontrar(Integer id) {
        return intervencaoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Intervenção", id));
    }
}
