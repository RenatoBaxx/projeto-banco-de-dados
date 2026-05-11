package com.projetoDados.HUB.Backend.Redis.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.projetoDados.HUB.Backend.Mongo.Model.Jogo;
import com.projetoDados.HUB.Backend.Mongo.Repository.JogoRepository;
import com.projetoDados.HUB.Backend.Redis.DTO.RankingJogoItemResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Espelho do catálogo Mongo no Redis: ordem de exibição gerada no warmup e hashes por jogo.
 * <p>
 * Chaves Redis:
 * <ul>
 * <li>{@code catalog:ranking:order} — lista com ordem dos ids (ranking de catálogo)</li>
 * <li>{@code catalog:ranking:ids} — set dos ids conhecidos no cache</li>
 * <li>{@code catalog:game:{id}} — hash com nome, preço, plataformas, etc.</li>
 * </ul>
 * Métricas ao vivo (online, ZSET de popularidade) ficam em {@link GameStatsService} ({@code game:{id}:stats},
 * {@link GameStatsService#RANKING_GAMES_ZSET}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogoJogoRankingService {

    private static final String ORDEM_KEY = "catalog:ranking:order";
    private static final String IDS_SET_KEY = "catalog:ranking:ids";
    private static final String HASH_PREFIX = "catalog:game:";

    private final JogoRepository jogoRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void recarregarDoMongoParaRedis() {
        List<Jogo> todos = jogoRepository.findAll();
        limparCatalogoAntigo();

        if (todos.isEmpty()) {
            log.info("Catalogo Redis: nenhum documento no Mongo; ranking vazio.");
            return;
        }

        List<Jogo> embaralhados = new ArrayList<>(todos);
        Collections.shuffle(embaralhados, ThreadLocalRandom.current());

        for (Jogo j : embaralhados) {
            String id = Objects.requireNonNull(j.getId(), "id Mongo nulo");
            gravarHashJogo(j);
            redisTemplate.opsForList().rightPush(ORDEM_KEY, id);
            redisTemplate.opsForSet().add(IDS_SET_KEY, id);
        }
        log.info("Catalogo Redis: {} jogo(s) indexados, ordem aleatoria.", embaralhados.size());
    }

    /**
     * Grava ou atualiza um jogo no Redis. Se for novo no catálogo, entra no fim do ranking.
     */
    public void sincronizarDocumentoNoRedis(Jogo j) {
        if (j == null || j.getId() == null) {
            return;
        }
        try {
            gravarHashJogo(j);
            Boolean existe = redisTemplate.opsForSet().isMember(IDS_SET_KEY, j.getId());
            if (!Boolean.TRUE.equals(existe)) {
                redisTemplate.opsForList().rightPush(ORDEM_KEY, j.getId());
                redisTemplate.opsForSet().add(IDS_SET_KEY, j.getId());
                log.debug("Catalogo Redis: novo id {} adicionado ao fim do ranking.", j.getId());
            }
        } catch (DataAccessException e) {
            log.warn("Catalogo Redis: falha ao sincronizar jogo id={} : {}", j.getId(), e.getMessage());
        }
    }

    public void removerDocumentoDoRedis(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        try {
            redisTemplate.delete(HASH_PREFIX + id);
            redisTemplate.opsForSet().remove(IDS_SET_KEY, id);
            redisTemplate.opsForList().remove(ORDEM_KEY, 1, id);
        } catch (DataAccessException e) {
            log.warn("Catalogo Redis: falha ao remover id={} : {}", id, e.getMessage());
        }
    }

    private void gravarHashJogo(Jogo j) {
        String id = Objects.requireNonNull(j.getId());
        String hashKey = HASH_PREFIX + id;
        redisTemplate.opsForHash().put(hashKey, "nome", str(j.getNome()));
        redisTemplate.opsForHash().put(hashKey, "descricao", str(j.getDescricao()));
        redisTemplate.opsForHash().put(hashKey, "preco", str(j.getPreco()));
        redisTemplate.opsForHash().put(hashKey, "modoJogo", str(j.getModoJogo()));
        redisTemplate.opsForHash().put(hashKey, "status", str(j.getStatus()));
        redisTemplate.opsForHash().put(hashKey, "arquivoNome", str(j.getArquivoNome()));
        redisTemplate.opsForHash().put(hashKey, "arquivoTamanhoBytes", Long.toString(j.getArquivoTamanhoBytes()));
        redisTemplate.opsForHash().put(hashKey, "arquivoCaminhoRelativo", str(j.getArquivoCaminhoRelativo()));
        redisTemplate.opsForHash().put(hashKey, "sistemas", joinListas(j.getSistemasOperacionais()));
        redisTemplate.opsForHash().put(hashKey, "plataformas", joinListas(j.getPlataformasPublicacao()));
        boolean capa = j.getImagemDados() != null && j.getImagemDados().length > 0;
        redisTemplate.opsForHash().put(hashKey, "temCapa", capa ? "1" : "0");
    }

    public List<RankingJogoItemResponse> listarRanking() {
        try {
            List<Object> idsObj = redisTemplate.opsForList().range(ORDEM_KEY, 0, -1);
            if (idsObj == null || idsObj.isEmpty()) {
                return List.of();
            }
            List<RankingJogoItemResponse> saida = new ArrayList<>();
            int pos = 1;
            for (Object idObj : idsObj) {
                String id = idObj.toString();
                RankingJogoItemResponse item = montarItem(pos++, id);
                if (item != null) {
                    saida.add(item);
                }
            }
            return saida;
        } catch (DataAccessException e) {
            log.warn("Redis indisponivel ao listar ranking: {}", e.getMessage());
            return List.of();
        }
    }

    private RankingJogoItemResponse montarItem(int posicao, String id) {
        String hashKey = HASH_PREFIX + id;
        try {
            if (Boolean.FALSE.equals(redisTemplate.hasKey(hashKey))) {
                return null;
            }
        } catch (DataAccessException e) {
            log.warn("Catalogo Redis: falha ao ler game id={}", id);
            return null;
        }

        var nome = getCampo(hashKey, "nome");
        var descricao = getCampo(hashKey, "descricao");
        var preco = getCampo(hashKey, "preco");
        var modoJogo = getCampo(hashKey, "modoJogo");
        var status = getCampo(hashKey, "status");
        var arquivoNome = getCampo(hashKey, "arquivoNome");
        long tamanho = parseLongo(getCampo(hashKey, "arquivoTamanhoBytes"));
        var sistemas = splitListas(getCampo(hashKey, "sistemas"));
        var plataformas = splitListas(getCampo(hashKey, "plataformas"));
        boolean capaDisponivel = "1".equals(getCampo(hashKey, "temCapa"));

        return new RankingJogoItemResponse(
                posicao,
                id,
                nome,
                descricao,
                preco,
                modoJogo,
                status,
                arquivoNome,
                tamanho,
                sistemas,
                plataformas,
                capaDisponivel);
    }

    private String getCampo(String hashKey, String campo) {
        Object v = redisTemplate.opsForHash().get(hashKey, campo);
        return v != null ? v.toString() : "";
    }

    private static long parseLongo(String s) {
        if (s == null || s.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void limparCatalogoAntigo() {
        try {
            var antigos = redisTemplate.opsForSet().members(IDS_SET_KEY);
            if (antigos != null) {
                for (Object o : antigos) {
                    redisTemplate.delete(HASH_PREFIX + o.toString());
                }
            }
        } catch (DataAccessException e) {
            log.debug("Catalogo Redis: nada a limpar ou Redis indisponivel: {}", e.getMessage());
        }
        redisTemplate.delete(ORDEM_KEY);
        redisTemplate.delete(IDS_SET_KEY);
    }

    private static String str(String s) {
        return s != null ? s : "";
    }

    /**
     * SO e plataformas vêm de listas fixas no UI; separador improvável nos rótulos.
     */
    private static String joinListas(List<String> lista) {
        if (lista == null || lista.isEmpty()) {
            return "";
        }
        return lista.stream().map(CatalogoJogoRankingService::sanitizeToken).collect(Collectors.joining("\u0001"));
    }

    private static List<String> splitListas(String armazenado) {
        if (armazenado == null || armazenado.isEmpty()) {
            return List.of();
        }
        String[] partes = armazenado.split("\u0001", -1);
        List<String> r = new ArrayList<>();
        for (String p : partes) {
            if (!p.isEmpty()) {
                r.add(p);
            }
        }
        return r;
    }

    private static String sanitizeToken(String s) {
        return s != null ? s.replace("\u0001", "") : "";
    }
}
