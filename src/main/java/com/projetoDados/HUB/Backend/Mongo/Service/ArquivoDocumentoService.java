package com.projetoDados.HUB.Backend.Mongo.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.projetoDados.HUB.Backend.Mongo.DTO.ArquivoDocumentoDashboardItemDTO;
import com.projetoDados.HUB.Backend.Mongo.Model.ArquivoDocumento;
import com.projetoDados.HUB.Backend.Mongo.Repository.ArquivoDocumentoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArquivoDocumentoService {

    private static final String STATUS_PENDENTE = "PENDENTE";

    private final ArquivoDocumentoRepository repository;

    @Value("${app.uploads.jogos-dir:uploads/games}")
    private String jogosDirRelativo;

    public ArquivoDocumento criar(ArquivoDocumento novo) {
        novo.setId(null);
        if (novo.getStatus() == null || novo.getStatus().isBlank()) {
            novo.setStatus(STATUS_PENDENTE);
        }
        return repository.save(novo);
    }

    public ArquivoDocumento criarComUpload(
            String nome,
            String descricao,
            String preco,
            String osJson,
            String modo,
            String platformsJson,
            MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("arquivo ausente ou vazio; esperado .zip, recebido=null");
        }
        if (!arquivo.getOriginalFilename().toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException(
                    "arquivo invalido; esperado extensao .zip, recebido=" + arquivo.getOriginalFilename());
        }
        List<String> os = parseListaStringJson(osJson, "os");
        List<String> plataformas = parseListaStringJson(platformsJson, "platforms");
        if (os.isEmpty()) {
            throw new IllegalArgumentException("os vazio; esperado lista JSON nao vazia, recebido=" + osJson);
        }
        if (plataformas.isEmpty()) {
            throw new IllegalArgumentException(
                    "platforms vazio; esperado lista JSON nao vazia, recebido=" + platformsJson);
        }

        ArquivoDocumento novo = new ArquivoDocumento();
        novo.setNome(nome);
        novo.setDescricao(descricao);
        novo.setPreco(preco);
        novo.setSistemasOperacionais(os);
        novo.setModoJogo(modo);
        novo.setPlataformasPublicacao(plataformas);
        novo.setArquivoNome(arquivo.getOriginalFilename());
        novo.setArquivoTamanhoBytes(arquivo.getSize());
        novo.setStatus(STATUS_PENDENTE);

        ArquivoDocumento salvo = repository.save(novo);
        Path destino = salvarArquivoEmDisco(salvo.getId(), arquivo);
        salvo.setArquivoCaminhoRelativo(destino.toString().replace("\\", "/"));
        return repository.save(salvo);
    }

    public List<ArquivoDocumento> listar() {
        return repository.findAll();
    }

    public List<ArquivoDocumentoDashboardItemDTO> listarParaDashboard() {
        return repository.findAll().stream().map(this::paraDashboardItem).collect(Collectors.toList());
    }

    public Optional<ArquivoDocumento> buscarPorId(String id) {
        return repository.findById(id);
    }

    public Optional<ArquivoDocumento> atualizar(String id, ArquivoDocumento atualizacao) {
        return repository.findById(id).map(existente -> salvarAtualizacao(existente, atualizacao));
    }

    public Optional<ArquivoDocumento> atualizarStatus(String id, String status) {
        return repository.findById(id).map(doc -> {
            doc.setStatus(status);
            return repository.save(doc);
        });
    }

    public boolean deletar(String id) {
        Optional<ArquivoDocumento> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        ArquivoDocumento doc = opt.get();
        apagarZipSeExistir(doc.getArquivoCaminhoRelativo());
        repository.deleteById(id);
        return true;
    }

    private ArquivoDocumento salvarAtualizacao(ArquivoDocumento existente, ArquivoDocumento atualizacao) {
        existente.setNome(atualizacao.getNome());
        existente.setDescricao(atualizacao.getDescricao());
        existente.setPreco(atualizacao.getPreco());
        existente.setSistemasOperacionais(atualizacao.getSistemasOperacionais());
        existente.setModoJogo(atualizacao.getModoJogo());
        existente.setPlataformasPublicacao(atualizacao.getPlataformasPublicacao());
        existente.setArquivoNome(atualizacao.getArquivoNome());
        existente.setArquivoTamanhoBytes(atualizacao.getArquivoTamanhoBytes());
        existente.setArquivoCaminhoRelativo(atualizacao.getArquivoCaminhoRelativo());
        existente.setStatus(atualizacao.getStatus());
        return repository.save(existente);
    }

    private ArquivoDocumentoDashboardItemDTO paraDashboardItem(ArquivoDocumento doc) {
        String loja = lojaPrincipal(doc.getPlataformasPublicacao());
        return new ArquivoDocumentoDashboardItemDTO(doc.getId(), loja,
                doc.getStatus() != null ? doc.getStatus() : STATUS_PENDENTE);
    }

    private static String lojaPrincipal(List<String> plataformas) {
        if (plataformas == null || plataformas.isEmpty()) {
            return "—";
        }
        return plataformas.get(0);
    }

    /**
     * Aceita arrays gerados por {@code JSON.stringify} no browser, ex. {@code ["Windows","Linux"]}.
     * Não depende de Jackson (evita falha de classpath no compile).
     */
    private static List<String> parseListaStringJson(String json, String campo) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException(campo + " ausente; esperado JSON array de strings");
        }
        String t = json.trim();
        if (!t.startsWith("[") || !t.endsWith("]")) {
            throw new IllegalArgumentException(campo + " invalido; esperado array JSON [...], recebido=" + json);
        }
        String inner = t.substring(1, t.length() - 1).trim();
        if (inner.isEmpty()) {
            return List.of();
        }
        List<String> resultado = new ArrayList<>();
        int i = 0;
        int n = inner.length();
        while (i < n) {
            while (i < n && Character.isWhitespace(inner.charAt(i))) {
                i++;
            }
            if (i < n && inner.charAt(i) == ',') {
                i++;
                continue;
            }
            while (i < n && Character.isWhitespace(inner.charAt(i))) {
                i++;
            }
            if (i >= n) {
                break;
            }
            if (inner.charAt(i) != '"') {
                throw new IllegalArgumentException(
                        campo + " invalido; cada item deve ser string entre aspas duplas, recebido=" + json);
            }
            i++;
            StringBuilder sb = new StringBuilder();
            boolean fechouAspas = false;
            while (i < n) {
                char c = inner.charAt(i);
                if (c == '\\' && i + 1 < n) {
                    sb.append(inner.charAt(i + 1));
                    i += 2;
                    continue;
                }
                if (c == '"') {
                    i++;
                    fechouAspas = true;
                    break;
                }
                sb.append(c);
                i++;
            }
            if (!fechouAspas) {
                throw new IllegalArgumentException(
                        campo + " invalido; string sem aspas de fechamento, recebido=" + json);
            }
            resultado.add(sb.toString());
        }
        return resultado;
    }

    private Path salvarArquivoEmDisco(String id, MultipartFile arquivo) {
        try {
            Path dir = Path.of(jogosDirRelativo);
            Files.createDirectories(dir);
            Path destino = dir.resolve(id + ".zip");
            arquivo.transferTo(destino);
            return destino.toAbsolutePath().normalize();
        } catch (IOException e) {
            throw new IllegalStateException("falha ao gravar zip no disco para id=" + id + ": " + e.getMessage(), e);
        }
    }

    private static void apagarZipSeExistir(String caminho) {
        if (caminho == null) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(caminho));
        } catch (IOException ignored) {
        }
    }
}
