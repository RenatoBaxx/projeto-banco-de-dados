package com.projetoDados.HUB.Backend.Mongo.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.projetoDados.HUB.Backend.Mongo.DTO.ImagemPayload;
import com.projetoDados.HUB.Backend.Mongo.DTO.JogoDashboardItemDTO;
import com.projetoDados.HUB.Backend.Mongo.Model.Jogo;
import com.projetoDados.HUB.Backend.Mongo.Repository.JogoRepository;
import com.projetoDados.HUB.Backend.Redis.Service.CatalogoJogoRankingService;

import lombok.RequiredArgsConstructor;

/**
 * Regras de negócio dos jogos no MongoDB e ficheiros .zip no disco.
 * <p>
 * <b>O que entrega:</b> persistência em {@link JogoRepository}, gravação de zip em
 * {@code app.uploads.jogos-dir}, validação de capa, e após cada create/update/delete chama o Redis
 * ({@link CatalogoJogoRankingService}) para manter o espelho {@code catalog:*}.
 */
@Service
@RequiredArgsConstructor
public class JogoService {

    private static final String STATUS_PENDENTE = "PENDENTE";

    private static final long IMAGEM_MAX_BYTES = 5 * 1024 * 1024L;

    private static final Set<String> IMAGEM_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif");

    private final JogoRepository repository;
    private final CatalogoJogoRankingService catalogoJogoRankingService;

    @Value("${app.uploads.jogos-dir:uploads/games}")
    private String jogosDirRelativo;

    // ---------- Criação ----------

    /** Insere documento JSON; status default PENDENTE; sincroniza Redis. */
    public Jogo criar(Jogo novo) {
        novo.setId(null);
        if (novo.getStatus() == null || novo.getStatus().isBlank()) {
            novo.setStatus(STATUS_PENDENTE);
        }
        Jogo salvo = repository.save(novo);
        catalogoJogoRankingService.sincronizarDocumentoNoRedis(salvo);
        return salvo;
    }

    /**
     * Cria jogo com multipart: valida capa (tipo/tamanho), listas JSON em {@code os} e {@code platforms},
     * grava Mongo, depois grava {@code id}.zip no disco e atualiza caminho; sincroniza Redis.
     */
    public Jogo criarComUpload(
            String nome,
            String descricao,
            String empresaId,
            String preco,
            String osJson,
            String modo,
            String platformsJson,
            MultipartFile imagem,
            MultipartFile arquivo) {
        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("imagem ausente ou vazia; esperado jpeg/png/webp/gif, recebido=null");
        }
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("arquivo ausente ou vazio; esperado .zip, recebido=null");
        }
        if (!arquivo.getOriginalFilename().toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException(
                    "arquivo invalido; esperado extensao .zip, recebido=" + arquivo.getOriginalFilename());
        }
        ImagemPayload capa = lerImagemValidada(imagem);
        List<String> os = parseListaStringJson(osJson, "os");
        List<String> plataformas = parseListaStringJson(platformsJson, "platforms");
        if (os.isEmpty()) {
            throw new IllegalArgumentException("os vazio; esperado lista JSON nao vazia, recebido=" + osJson);
        }
        if (plataformas.isEmpty()) {
            throw new IllegalArgumentException(
                    "platforms vazio; esperado lista JSON nao vazia, recebido=" + platformsJson);
        }

        Jogo novo = new Jogo();
        novo.setNome(nome);
        novo.setDescricao(descricao);
        novo.setEmpresaId(empresaId);
        novo.setPreco(preco);
        novo.setSistemasOperacionais(os);
        novo.setModoJogo(modo);
        novo.setPlataformasPublicacao(plataformas);
        novo.setArquivoNome(arquivo.getOriginalFilename());
        novo.setArquivoTamanhoBytes(arquivo.getSize());
        novo.setStatus(STATUS_PENDENTE);
        novo.setImagemNome(imagem.getOriginalFilename());
        novo.setImagemContentType(capa.contentType());
        novo.setImagemDados(capa.dados());

        Jogo salvo = repository.save(novo);
        Path destino = salvarArquivoEmDisco(salvo.getId(), arquivo);
        salvo.setArquivoCaminhoRelativo(destino.toString().replace("\\", "/"));
        Jogo finalDoc = repository.save(salvo);
        catalogoJogoRankingService.sincronizarDocumentoNoRedis(finalDoc);
        return finalDoc;
    }

    // ---------- Leitura ----------

    public List<Jogo> listar() {
        return repository.findAll();
    }

    public List<JogoDashboardItemDTO> listarParaDashboard(String empresaId) {        
        return repository.findByEmpresaId(empresaId).stream().map(this::paraDashboardItem).collect(Collectors.toList());
    }

    public Optional<Jogo> buscarPorId(String id) {
        return repository.findById(id);
    }

    public Optional<ImagemPayload> buscarImagem(String id) {
        return repository.findById(id)
                .filter(d -> d.getImagemDados() != null && d.getImagemDados().length > 0)
                .map(d -> new ImagemPayload(
                        d.getImagemDados(),
                        d.getImagemContentType() != null && !d.getImagemContentType().isBlank()
                                ? d.getImagemContentType()
                                : "application/octet-stream"));
    }

    // ---------- Atualização e remoção ----------

    public Optional<Jogo> atualizar(String id, Jogo atualizacao) {
        return repository.findById(id).map(existente -> {
            Jogo salvo = salvarAtualizacao(existente, atualizacao);
            catalogoJogoRankingService.sincronizarDocumentoNoRedis(salvo);
            return salvo;
        });
    }

    public Optional<Jogo> atualizarStatus(String id, String status) {
        return repository.findById(id).map(doc -> {
            doc.setStatus(status);
            Jogo salvo = repository.save(doc);
            catalogoJogoRankingService.sincronizarDocumentoNoRedis(salvo);
            return salvo;
        });
    }

    public boolean deletar(String id) {
        Optional<Jogo> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        Jogo doc = opt.get();
        apagarZipSeExistir(doc.getArquivoCaminhoRelativo());
        repository.deleteById(id);
        catalogoJogoRankingService.removerDocumentoDoRedis(id);
        return true;
    }

    // ---------- Internos ----------

    private Jogo salvarAtualizacao(Jogo existente, Jogo atualizacao) {
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
        existente.setImagemNome(atualizacao.getImagemNome());
        existente.setImagemContentType(atualizacao.getImagemContentType());
        existente.setImagemDados(atualizacao.getImagemDados());
        return repository.save(existente);
    }

    private JogoDashboardItemDTO paraDashboardItem(Jogo doc) {
        String loja = lojaPrincipal(doc.getPlataformasPublicacao());
        return new JogoDashboardItemDTO(doc.getId(), doc.getNome(), loja,
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

    private static ImagemPayload lerImagemValidada(MultipartFile imagem) {
        String ct = imagem.getContentType() != null
                ? imagem.getContentType().toLowerCase(Locale.ROOT).split(";")[0].trim()
                : "";
        if (!IMAGEM_TYPES.contains(ct)) {
            throw new IllegalArgumentException(
                    "imagem invalida; esperado Content-Type image/jpeg|image/png|image/webp|image/gif, recebido="
                            + imagem.getContentType());
        }
        if (imagem.getSize() > IMAGEM_MAX_BYTES) {
            throw new IllegalArgumentException(
                    "imagem muito grande; max=" + IMAGEM_MAX_BYTES + " bytes, recebido=" + imagem.getSize());
        }
        try {
            return new ImagemPayload(imagem.getBytes(), ct);
        } catch (IOException e) {
            throw new IllegalStateException("falha ao ler bytes da imagem: " + e.getMessage(), e);
        }
    }
}
