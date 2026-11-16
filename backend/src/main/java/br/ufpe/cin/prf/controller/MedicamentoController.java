package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.MedicamentoRequest;
import br.ufpe.cin.prf.dto.MedicamentoResponse;
import br.ufpe.cin.prf.service.MedicamentoService;
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
@RequestMapping("/api/medicamentos")
@Tag(name = "Medicamentos")
public class MedicamentoController {

    private final MedicamentoService service;

    public MedicamentoController(MedicamentoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista medicamentos (filtro opcional: cpfPaciente)")
    public List<MedicamentoResponse> listar(@RequestParam(required = false) String cpfPaciente) {
        return service.listar(cpfPaciente);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um medicamento pelo id")
    public MedicamentoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um medicamento em uso por um paciente")
    public ResponseEntity<MedicamentoResponse> criar(@Valid @RequestBody MedicamentoRequest req) {
        MedicamentoResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/medicamentos/" + criado.idMedicamento())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um medicamento")
    public MedicamentoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody MedicamentoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um medicamento (bloqueado se for alvo de suspeitas)")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
