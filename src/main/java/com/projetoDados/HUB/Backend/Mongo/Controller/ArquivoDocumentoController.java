package com.projetoDados.HUB.Backend.Mongo.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projetoDados.HUB.Backend.Mongo.Model.ArquivoDocumento;
import com.projetoDados.HUB.Backend.Mongo.Service.ArquivoDocumentoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mongo/arquivos")
@RequiredArgsConstructor
public class ArquivoDocumentoController {

    private final ArquivoDocumentoService service;

    @PostMapping
    public ResponseEntity<ArquivoDocumento> criar(@RequestBody ArquivoDocumento body) {
        return ResponseEntity.ok(service.criar(body));
    }

    @GetMapping
    public ResponseEntity<List<ArquivoDocumento>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArquivoDocumento> buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArquivoDocumento> atualizar(
            @PathVariable String id,
            @RequestBody ArquivoDocumento body) {
        return service.atualizar(id, body)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        if (!service.deletar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}

