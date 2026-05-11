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

import com.projetoDados.HUB.Backend.Mongo.DTO.JogoDashboardItemDTO;
import com.projetoDados.HUB.Backend.Mongo.Model.Jogo;
import com.projetoDados.HUB.Backend.Mongo.Service.JogoService;

import lombok.RequiredArgsConstructor;

/**
 * API REST dos jogos (documentos MongoDB + ficheiros em disco).
 * <p>
 * Expõe CRUD, publicação com multipart (capa + zip) e bytes da imagem de capa.
 * Cada alteração relevante sincroniza o cache Redis do catálogo ({@code catalog:*}) via serviço.
 */
@RestController
@RequestMapping("/api/jogos")
@RequiredArgsConstructor
public class JogoController {

    private final JogoService service;

    /** Cria jogo só com JSON (sem upload de ficheiros). Entrega: documento salvo (201 implícito em 200). */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Jogo> criarJson(@RequestBody Jogo body) {
        return ResponseEntity.ok(service.criar(body));
    }

    /**
     * Publica jogo com capa e .zip. Entrega: {@code id} + mensagem, ou 400 com {@code error}.
     */
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
            Jogo salvo = service.criarComUpload(
                    nome, descricao, preco, osJson, modo, platformsJson, imagem, arquivo);
            return ResponseEntity.ok(Map.of("id", salvo.getId(), "message", "Jogo registrado com sucesso"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Lista resumo para o painel (id, loja, status). */
    @GetMapping("/dashboard")
    public ResponseEntity<List<JogoDashboardItemDTO>> listarDashboard() {
        return ResponseEntity.ok(service.listarParaDashboard());
    }

    /** Lista todos os documentos completos (atenção: inclui metadados; imagem em bytes pode ser omitida no JSON conforme serialização). */
    @GetMapping
    public ResponseEntity<List<Jogo>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    /** Entrega bytes da capa e Content-Type, ou 404. */
    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> getImagemCapa(@PathVariable String id) {
        return service.buscarImagem(id)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(p.contentType()))
                        .body(p.dados()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Busca um documento por id, ou 404. */
    @GetMapping("/{id}")
    public ResponseEntity<Jogo> buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Atualização completa por JSON. Entrega documento atualizado ou 404. */
    @PutMapping("/{id}")
    public ResponseEntity<Jogo> atualizar(
            @PathVariable String id,
            @RequestBody Jogo body) {
        return service.atualizar(id, body)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Atualiza só o campo status. 200 vazio ou 404. */
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable String id,
            @RequestParam("status") String status) {
        Optional<Jogo> ok = service.atualizarStatus(id, status);
        return ok.isPresent() ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /** Remove Mongo + zip em disco + entrada no Redis catálogo. 200 ou 404. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        if (!service.deletar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
