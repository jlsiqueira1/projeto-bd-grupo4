package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.ProfissionalSaudeRequest;
import br.ufpe.cin.prf.dto.ProfissionalSaudeResponse;
import br.ufpe.cin.prf.service.ProfissionalSaudeService;
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
@RequestMapping("/api/profissionais")
@Tag(name = "Profissionais de saúde")
public class ProfissionalSaudeController {

    private final ProfissionalSaudeService service;

    public ProfissionalSaudeController(ProfissionalSaudeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os profissionais de saúde")
    public List<ProfissionalSaudeResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{cpf}")
    @Operation(summary = "Busca um profissional pelo CPF")
    public ProfissionalSaudeResponse buscar(@PathVariable String cpf) {
        return service.buscar(cpf);
    }

    @PostMapping
    @Operation(summary = "Cadastra um profissional (insere em pessoa e profissional_saude)")
    public ResponseEntity<ProfissionalSaudeResponse> criar(@Valid @RequestBody ProfissionalSaudeRequest req) {
        ProfissionalSaudeResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/profissionais/" + criado.cpf())).body(criado);
    }

    @PutMapping("/{cpf}")
    @Operation(summary = "Atualiza um profissional (o CPF do corpo é ignorado)")
    public ProfissionalSaudeResponse atualizar(@PathVariable String cpf,
                                               @Valid @RequestBody ProfissionalSaudeRequest req) {
        return service.atualizar(cpf, req);
    }

    @DeleteMapping("/{cpf}")
    @Operation(summary = "Exclui um profissional (bloqueado se tiver avaliações ou consultas)")
    public ResponseEntity<Void> excluir(@PathVariable String cpf) {
        service.excluir(cpf);
        return ResponseEntity.noContent().build();
    }
}
