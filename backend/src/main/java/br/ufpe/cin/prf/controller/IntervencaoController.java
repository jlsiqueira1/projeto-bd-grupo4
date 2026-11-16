package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.IntervencaoRequest;
import br.ufpe.cin.prf.dto.IntervencaoResponse;
import br.ufpe.cin.prf.service.IntervencaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/intervencoes")
@Tag(name = "Intervenções")
public class IntervencaoController {

    private final IntervencaoService service;

    public IntervencaoController(IntervencaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista intervenções (filtro opcional: idSuspeita)")
    public List<IntervencaoResponse> listar(@RequestParam(required = false) Integer idSuspeita) {
        return service.listar(idSuspeita);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma intervenção pelo id")
    public IntervencaoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Registra uma intervenção para uma suspeita")
    public ResponseEntity<IntervencaoResponse> criar(@Valid @RequestBody IntervencaoRequest req) {
        IntervencaoResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/intervencoes/" + criado.idIntervencao())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma intervenção")
    public IntervencaoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody IntervencaoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma intervenção")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
