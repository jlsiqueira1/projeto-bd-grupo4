package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.AtualizarCpfRequest;
import br.ufpe.cin.prf.dto.PessoaResponse;
import br.ufpe.cin.prf.service.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Visão da superclasse. Criar/editar/excluir é feito pelas subclasses
 * (/api/pacientes, /api/profissionais, /api/estagiarios).
 */
@RestController
@RequestMapping("/api/pessoas")
@Tag(name = "Pessoas", description = "Consulta da superclasse e correção de CPF")
public class PessoaController {

    private final PessoaService service;

    public PessoaController(PessoaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todas as pessoas, com o tipo de cada uma")
    public List<PessoaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{cpf}")
    @Operation(summary = "Busca uma pessoa pelo CPF")
    public PessoaResponse buscar(@PathVariable String cpf) {
        return service.buscar(cpf);
    }

    @PatchMapping("/{cpf}/cpf")
    @Operation(summary = "Corrige o CPF (propagado a todas as tabelas por ON UPDATE CASCADE)")
    public PessoaResponse atualizarCpf(@PathVariable String cpf, @Valid @RequestBody AtualizarCpfRequest req) {
        return service.atualizarCpf(cpf, req.novoCpf());
    }
}
