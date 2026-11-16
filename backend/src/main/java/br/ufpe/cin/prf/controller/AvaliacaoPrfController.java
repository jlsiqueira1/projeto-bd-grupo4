package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.AvaliacaoPrfRequest;
import br.ufpe.cin.prf.dto.AvaliacaoPrfResponse;
import br.ufpe.cin.prf.service.AvaliacaoPrfService;
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
@RequestMapping("/api/avaliacoes")
@Tag(name = "Avaliações PRF")
public class AvaliacaoPrfController {

    private final AvaliacaoPrfService service;

    public AvaliacaoPrfController(AvaliacaoPrfService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista avaliações, mais recentes primeiro (filtro opcional: cpfPaciente)")
    public List<AvaliacaoPrfResponse> listar(@RequestParam(required = false) String cpfPaciente) {
        return service.listar(cpfPaciente);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma avaliação pelo id")
    public AvaliacaoPrfResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Registra uma avaliação (profissional, ou estagiário + supervisor)")
    public ResponseEntity<AvaliacaoPrfResponse> criar(@Valid @RequestBody AvaliacaoPrfRequest req) {
        AvaliacaoPrfResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/avaliacoes/" + criado.idAvaliacao())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma avaliação")
    public AvaliacaoPrfResponse atualizar(@PathVariable Integer id, @Valid @RequestBody AvaliacaoPrfRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma avaliação (bloqueado se tiver suspeitas ou retorno)")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
