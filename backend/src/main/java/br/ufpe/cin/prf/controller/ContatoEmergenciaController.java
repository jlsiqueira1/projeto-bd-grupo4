package br.ufpe.cin.prf.controller;

import br.ufpe.cin.prf.dto.ContatoEmergenciaRequest;
import br.ufpe.cin.prf.dto.ContatoEmergenciaResponse;
import br.ufpe.cin.prf.service.ContatoEmergenciaService;
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
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Entidade fraca: os endpoints ficam aninhados no paciente dono.
 * A chave do contato é (cpf do paciente, nome), por isso o nome aparece na URL
 * (com espaços codificados, ex.: /contatos/Paulo%20Silva).
 */
@RestController
@RequestMapping("/api/pacientes/{cpf}/contatos")
@Tag(name = "Contatos de emergência")
public class ContatoEmergenciaController {

    private final ContatoEmergenciaService service;

    public ContatoEmergenciaController(ContatoEmergenciaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os contatos de emergência de um paciente")
    public List<ContatoEmergenciaResponse> listar(@PathVariable String cpf) {
        return service.listar(cpf);
    }

    @GetMapping("/{nome}")
    @Operation(summary = "Busca um contato pelo nome")
    public ContatoEmergenciaResponse buscar(@PathVariable String cpf, @PathVariable String nome) {
        return service.buscar(cpf, nome);
    }

    @PostMapping
    @Operation(summary = "Adiciona um contato de emergência ao paciente")
    public ResponseEntity<ContatoEmergenciaResponse> criar(@PathVariable String cpf,
                                                           @Valid @RequestBody ContatoEmergenciaRequest req) {
        ContatoEmergenciaResponse criado = service.criar(cpf, req);
        String nomeUrl = UriUtils.encodePathSegment(criado.nome(), StandardCharsets.UTF_8);
        return ResponseEntity.created(URI.create("/api/pacientes/" + cpf + "/contatos/" + nomeUrl)).body(criado);
    }

    @PutMapping("/{nome}")
    @Operation(summary = "Atualiza telefone e relação de um contato (o nome é a chave e não muda)")
    public ContatoEmergenciaResponse atualizar(@PathVariable String cpf, @PathVariable String nome,
                                               @Valid @RequestBody ContatoEmergenciaRequest req) {
        return service.atualizar(cpf, nome, req);
    }

    @DeleteMapping("/{nome}")
    @Operation(summary = "Remove um contato de emergência")
    public ResponseEntity<Void> excluir(@PathVariable String cpf, @PathVariable String nome) {
        service.excluir(cpf, nome);
        return ResponseEntity.noContent().build();
    }
}
