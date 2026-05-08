package com.projetoDados.HUB.Backend.Mongo.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.projetoDados.HUB.Backend.Mongo.DTO.ArquivoDocumentoDashboardItemDTO;
import com.projetoDados.HUB.Backend.Mongo.Model.ArquivoDocumento;
import com.projetoDados.HUB.Backend.Mongo.Service.ArquivoDocumentoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mongo/arquivos")
@RequiredArgsConstructor
public class ArquivoDocumentoController {

    private final ArquivoDocumentoService service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArquivoDocumento> criarJson(@RequestBody ArquivoDocumento body) {
        return ResponseEntity.ok(service.criar(body));
    }

    @PostMapping(value = "/publicar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> publicarComArquivo(
            @RequestParam("nome") String nome,
            @RequestParam("descricao") String descricao,
            @RequestParam("preco") String preco,
            @RequestParam("os") String osJson,
            @RequestParam("modo") String modo,
            @RequestParam("platforms") String platformsJson,
            @RequestParam("imagem") MultipartFile imagem,
            @RequestParam("arquivo") MultipartFile arquivo) {
        try {
            ArquivoDocumento salvo = service.criarComUpload(
                    nome, descricao, preco, osJson, modo, platformsJson, imagem, arquivo);
            return ResponseEntity.ok(Map.of("id", salvo.getId(), "message", "Jogo registrado com sucesso"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<ArquivoDocumentoDashboardItemDTO>> listarDashboard() {
        return ResponseEntity.ok(service.listarParaDashboard());
    }

    @GetMapping
    public ResponseEntity<List<ArquivoDocumento>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> getImagemCapa(@PathVariable String id) {
        return service.buscarImagem(id)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(p.contentType()))
                        .body(p.dados()))
                .orElseGet(() -> ResponseEntity.notFound().build());
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

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable String id,
            @RequestParam("status") String status) {
        Optional<ArquivoDocumento> ok = service.atualizarStatus(id, status);
        return ok.isPresent() ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        if (!service.deletar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
