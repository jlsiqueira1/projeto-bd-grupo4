package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.PacienteRequest;
import br.ufpe.cin.prf.dto.PacienteResponse;
import br.ufpe.cin.prf.service.PacienteService;
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
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os pacientes")
    public List<PacienteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{cpf}")
    @Operation(summary = "Busca um paciente pelo CPF")
    public PacienteResponse buscar(@PathVariable String cpf) {
        return service.buscar(cpf);
    }

    @PostMapping
    @Operation(summary = "Cadastra um paciente (insere em pessoa e paciente)")
    public ResponseEntity<PacienteResponse> criar(@Valid @RequestBody PacienteRequest req) {
        PacienteResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/pacientes/" + criado.cpf())).body(criado);
    }

    @PutMapping("/{cpf}")
    @Operation(summary = "Atualiza os dados de um paciente (o CPF do corpo é ignorado)")
    public PacienteResponse atualizar(@PathVariable String cpf, @Valid @RequestBody PacienteRequest req) {
        return service.atualizar(cpf, req);
    }

    @DeleteMapping("/{cpf}")
    @Operation(summary = "Exclui um paciente (bloqueado se houver histórico clínico)")
    public ResponseEntity<Void> excluir(@PathVariable String cpf) {
        service.excluir(cpf);
        return ResponseEntity.noContent().build();
    }
}
