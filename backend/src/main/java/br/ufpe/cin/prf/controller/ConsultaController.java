package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.ConsultaRequest;
import br.ufpe.cin.prf.dto.ConsultaResponse;
import br.ufpe.cin.prf.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@Tag(name = "Consultas (agenda)")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Agenda de consultas (filtros opcionais: cpfPaciente, ou inicio/fim no formato AAAA-MM-DD)")
    public List<ConsultaResponse> listar(
            @RequestParam(required = false) String cpfPaciente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.listar(cpfPaciente, inicio, fim);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma consulta pelo id")
    public ConsultaResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Agenda uma consulta (PRIMEIRA, ou RETORNO ligado a uma avaliação)")
    public ResponseEntity<ConsultaResponse> criar(@Valid @RequestBody ConsultaRequest req) {
        ConsultaResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/consultas/" + criado.idConsulta())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma consulta")
    public ConsultaResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ConsultaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma consulta")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
