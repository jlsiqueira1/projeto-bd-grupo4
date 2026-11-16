package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.EstagiarioRequest;
import br.ufpe.cin.prf.dto.EstagiarioResponse;
import br.ufpe.cin.prf.service.EstagiarioService;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/estagiarios")
@Tag(name = "Estagiários")
public class EstagiarioController {

    private final EstagiarioService service;

    public EstagiarioController(EstagiarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os estagiários")
    public List<EstagiarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{cpf}")
    @Operation(summary = "Busca um estagiário pelo CPF")
    public EstagiarioResponse buscar(@PathVariable String cpf) {
        return service.buscar(cpf);
    }

    @PostMapping
    @Operation(summary = "Cadastra um estagiário (insere em pessoa e estagiario)")
    public ResponseEntity<EstagiarioResponse> criar(@Valid @RequestBody EstagiarioRequest req) {
        EstagiarioResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/estagiarios/" + criado.cpf())).body(criado);
    }

    @PutMapping("/{cpf}")
    @Operation(summary = "Atualiza um estagiário (o CPF do corpo é ignorado)")
    public EstagiarioResponse atualizar(@PathVariable String cpf, @Valid @RequestBody EstagiarioRequest req) {
        return service.atualizar(cpf, req);
    }

    @DeleteMapping("/{cpf}")
    @Operation(summary = "Exclui um estagiário (bloqueado se tiver avaliações ou consultas)")
    public ResponseEntity<Void> excluir(@PathVariable String cpf) {
        service.excluir(cpf);
        return ResponseEntity.noContent().build();
    }
}
