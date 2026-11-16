package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.SuspeitaPrfRequest;
import br.ufpe.cin.prf.dto.SuspeitaPrfResponse;
import br.ufpe.cin.prf.service.SuspeitaPrfService;
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
@RequestMapping("/api/suspeitas")
@Tag(name = "Suspeitas PRF")
public class SuspeitaPrfController {

    private final SuspeitaPrfService service;

    public SuspeitaPrfController(SuspeitaPrfService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista suspeitas (filtro opcional: idAvaliacao)")
    public List<SuspeitaPrfResponse> listar(@RequestParam(required = false) Integer idAvaliacao) {
        return service.listar(idAvaliacao);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma suspeita pelo id")
    public SuspeitaPrfResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Registra uma suspeita (medicamento deve ser do paciente da avaliação)")
    public ResponseEntity<SuspeitaPrfResponse> criar(@Valid @RequestBody SuspeitaPrfRequest req) {
        SuspeitaPrfResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/suspeitas/" + criado.idSuspeita())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma suspeita")
    public SuspeitaPrfResponse atualizar(@PathVariable Integer id, @Valid @RequestBody SuspeitaPrfRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma suspeita (bloqueado se tiver intervenções)")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
