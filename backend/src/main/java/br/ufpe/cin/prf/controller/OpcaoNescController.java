package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.OpcaoNescRequest;
import br.ufpe.cin.prf.dto.OpcaoNescResponse;
import br.ufpe.cin.prf.entity.CategoriaNesc;
import br.ufpe.cin.prf.service.OpcaoNescService;
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
@RequestMapping("/api/opcoes-nesc")
@Tag(name = "Catálogo NESC")
public class OpcaoNescController {

    private final OpcaoNescService service;

    public OpcaoNescController(OpcaoNescService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista o catálogo NESC (filtro opcional: categoria)")
    public List<OpcaoNescResponse> listar(@RequestParam(required = false) CategoriaNesc categoria) {
        return service.listar(categoria);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma opção NESC pelo id")
    public OpcaoNescResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra uma opção no catálogo NESC")
    public ResponseEntity<OpcaoNescResponse> criar(@Valid @RequestBody OpcaoNescRequest req) {
        OpcaoNescResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/opcoes-nesc/" + criado.idOpcaoNesc())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma opção NESC")
    public OpcaoNescResponse atualizar(@PathVariable Integer id, @Valid @RequestBody OpcaoNescRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma opção NESC (bloqueado se já classificou suspeitas)")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
