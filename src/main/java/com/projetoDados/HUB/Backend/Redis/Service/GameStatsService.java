package com.projetoDados.HUB.Backend.Redis.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.projetoDados.HUB.Backend.Mongo.Model.Jogo;
import com.projetoDados.HUB.Backend.Mongo.Repository.JogoRepository;
import com.projetoDados.HUB.Backend.Redis.DTO.JogoRedisPublicoResponse;
import com.projetoDados.HUB.Backend.Redis.Model.GameStats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Operações Redis por jogo: hashes {@code game:{id}:stats}, set {@code game:{id}:users}, ZSET
 * {@link #RANKING_GAMES_ZSET}. Inclui também o ranking/catálogo enriquecidos com dados do Mongo.
 * <p>
 * <b>O que entrega:</b> leitura/escrita de contagens e popularidade; listas agregadas para a API
 * {@code /api/stats} (ranking por atividade e catálogo com métricas).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameStatsService {

    private static final int PADRAIO_LIMITE_RANKING = 80;

    /** ZSET: ordenação por “quente” — incrementado em cada {@link #playerEnter}. */
    public static final String RANKING_GAMES_ZSET = "ranking:games";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JogoRepository jogoRepository;

    // ---------- Sessão e contadores por jogo ----------

    public void playerEnter(String gameId, String userId) {

        String statsKey = "game:" + gameId + ":stats";
        String usersKey = "game:" + gameId + ":users";

        redisTemplate.opsForSet().add(usersKey, userId);

        Object onlineObj = redisTemplate.opsForHash().get(statsKey, "online");
        Long online = onlineObj != null ? Long.parseLong(onlineObj.toString()) : 0L;

        online++;

        redisTemplate.opsForHash().put(statsKey, "online", online.toString());

        Object maxObj = redisTemplate.opsForHash().get(statsKey, "max");
        Long max = maxObj != null ? Long.parseLong(maxObj.toString()) : 0L;

        if (online > max) {
            redisTemplate.opsForHash().put(statsKey, "max", online.toString());
        }

        Object minObj = redisTemplate.opsForHash().get(statsKey, "min");
        if (minObj == null) {
            redisTemplate.opsForHash().put(statsKey, "min", online.toString());
        }

        redisTemplate.opsForZSet().incrementScore(RANKING_GAMES_ZSET, gameId, 1);
    }

    public void playerLeave(String gameId, String userId) {

        String statsKey = "game:" + gameId + ":stats";
        String usersKey = "game:" + gameId + ":users";

        redisTemplate.opsForSet().remove(usersKey, userId);

        Object onlineObj = redisTemplate.opsForHash().get(statsKey, "online");
        Long online = onlineObj != null ? Long.parseLong(onlineObj.toString()) : 0L;

        if (online > 0) {
            online--;
        }

        redisTemplate.opsForHash().put(statsKey, "online", online.toString());
    }

    public GameStats getStats(String gameId) {

        String statsKey = "game:" + gameId + ":stats";

        Map<Object, Object> data = redisTemplate.opsForHash().entries(statsKey);

        if (data.isEmpty()) return null;

        Long online = data.get("online") != null
                ? Long.parseLong(data.get("online").toString())
                : 0L;

        Long max = data.get("max") != null
                ? Long.parseLong(data.get("max").toString())
                : 0L;

        Long min = data.get("min") != null
                ? Long.parseLong(data.get("min").toString())
                : 0L;

        return GameStats.builder()
                .gameId(gameId)
                .online(online)
                .max(max)
                .min(min)
                .build();
    }

    public Set<Object> getOnlineUsers(String gameId) {
        return redisTemplate.opsForSet().members("game:" + gameId + ":users");
    }

    public Set<Object> getTopGames() {
        return redisTemplate.opsForZSet().reverseRange(RANKING_GAMES_ZSET, 0, 9);
    }

    public void definirBaselineSimulacao(String gameId, long online) {
        String statsKey = "game:" + gameId + ":stats";
        long v = Math.max(0L, online);
        redisTemplate.opsForHash().put(statsKey, "online", Long.toString(v));
        redisTemplate.opsForHash().put(statsKey, "max", Long.toString(v));
        redisTemplate.opsForHash().put(statsKey, "min", Long.toString(v));
    }

    public void definirOnlineSimulado(String gameId, long online) {
        String statsKey = "game:" + gameId + ":stats";
        long v = Math.max(0L, online);
        redisTemplate.opsForHash().put(statsKey, "online", Long.toString(v));

        Object maxObj = redisTemplate.opsForHash().get(statsKey, "max");
        long max = maxObj != null ? Long.parseLong(maxObj.toString()) : 0L;
        if (v > max) {
            redisTemplate.opsForHash().put(statsKey, "max", Long.toString(v));
        }

        Object minObj = redisTemplate.opsForHash().get(statsKey, "min");
        if (minObj == null) {
            redisTemplate.opsForHash().put(statsKey, "min", Long.toString(v));
        }
    }

    public void aplicarVariacaoAleatoriaOnline(String gameId, int limiteAbsoluto) {
        if (limiteAbsoluto < 0) {
            throw new IllegalArgumentException(
                    "limiteAbsoluto deve ser >= 0, recebido: " + limiteAbsoluto);
        }
        String statsKey = "game:" + gameId + ":stats";
        Object onlineObj = redisTemplate.opsForHash().get(statsKey, "online");
        long atual = onlineObj != null ? Long.parseLong(onlineObj.toString()) : 0L;

        int delta = limiteAbsoluto == 0
                ? 0
                : ThreadLocalRandom.current().nextInt(-limiteAbsoluto, limiteAbsoluto + 1);
        long novo = Math.max(0L, atual + delta);
        redisTemplate.opsForHash().put(statsKey, "online", Long.toString(novo));

        Object maxObj = redisTemplate.opsForHash().get(statsKey, "max");
        long max = maxObj != null ? Long.parseLong(maxObj.toString()) : 0L;
        if (novo > max) {
            redisTemplate.opsForHash().put(statsKey, "max", Long.toString(novo));
        }
    }

    // ---------- Ranking e catálogo com métricas (Mongo + Redis) ----------

    /** Ordem por score na ZSET; se vazia, fallback: jogos Mongo por nome com métricas. */
    public List<JogoRedisPublicoResponse> rankingPorAtividade(Integer limite) {
        int n = clampLimiteRanking(limite);
        try {
            Set<TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(RANKING_GAMES_ZSET, 0, n - 1);
            if (tuples != null && !tuples.isEmpty()) {
                return montarRankingDesdeZset(tuples, n);
            }
        } catch (DataAccessException e) {
            log.warn("Ranking Redis ZSET indisponivel: {}", e.getMessage());
        }
        return fallbackCatalogoOrdenado(n);
    }

    /** Todos os jogos no Mongo, ordenados por nome, cada linha com online/pico/score da ZSET. */
    public List<JogoRedisPublicoResponse> catalogoComMetricas() {
        List<Jogo> docs = jogoRepository.findAll().stream()
                .sorted(Comparator.comparing(this::nomeOuId))
                .collect(Collectors.toList());
        List<JogoRedisPublicoResponse> lista = new ArrayList<>();
        for (Jogo d : docs) {
            lista.add(montarLinhaCatalogoSemPosicao(d));
        }
        return lista;
    }

    private List<JogoRedisPublicoResponse> montarRankingDesdeZset(Set<TypedTuple<Object>> tuples, int limiteTotal) {
        int posicao = 1;
        List<JogoRedisPublicoResponse> resultado = new ArrayList<>();
        for (TypedTuple<Object> t : tuples) {
            if (t == null || t.getValue() == null) {
                continue;
            }
            if (resultado.size() >= limiteTotal) {
                break;
            }
            String id = t.getValue().toString();
            Double redisScore = t.getScore();
            double score = redisScore != null ? redisScore : 0.0;
            Optional<Jogo> doc = jogoRepository.findById(id);
            GameStats st = getStats(id);
            resultado.add(montarLinhaCompleta(posicao++, id, doc.orElse(null), st, score));
        }
        return resultado;
    }

    private List<JogoRedisPublicoResponse> fallbackCatalogoOrdenado(int limite) {
        List<Jogo> docs = jogoRepository.findAll().stream()
                .sorted(Comparator.comparing(this::nomeOuId))
                .limit(limite)
                .collect(Collectors.toList());
        List<JogoRedisPublicoResponse> lista = new ArrayList<>();
        int p = 1;
        for (Jogo d : docs) {
            Double z = safeZscore(d.getId());
            double score = z != null ? z : 0.0;
            GameStats st = getStats(d.getId());
            lista.add(montarLinhaCompleta(p++, d.getId(), d, st, score));
        }
        return lista;
    }

    private JogoRedisPublicoResponse montarLinhaCatalogoSemPosicao(Jogo d) {
        GameStats st = getStats(d.getId());
        Double z = safeZscore(d.getId());
        double score = z != null ? z : 0.0;
        return montarLinhaCompleta(null, d.getId(), d, st, score);
    }

    private JogoRedisPublicoResponse montarLinhaCompleta(
            Integer posicao,
            String id,
            Jogo d,
            GameStats st,
            double scorePopularidade) {
        Long onlineBx = st == null ? null : st.getOnline();
        Long picoBx = st == null ? null : st.getMax();
        long online = Objects.requireNonNullElse(onlineBx, 0L);
        long pico = Objects.requireNonNullElse(picoBx, 0L);

        if (d == null) {
            List<String> vazio = List.of();
            return new JogoRedisPublicoResponse(
                    posicao,
                    id,
                    "Jogo " + id,
                    "",
                    "",
                    "",
                    "",
                    false,
                    0L,
                    vazio,
                    vazio,
                    online,
                    pico,
                    scorePopularidade);
        }

        boolean capa = d.getImagemDados() != null && d.getImagemDados().length > 0;
        List<String> plats = coalesceLista(d.getPlataformasPublicacao());
        List<String> sis = coalesceLista(d.getSistemasOperacionais());

        return new JogoRedisPublicoResponse(
                posicao,
                d.getId(),
                str(d.getNome()),
                str(d.getDescricao()),
                str(d.getPreco()),
                str(d.getModoJogo()),
                str(d.getStatus()),
                capa,
                d.getArquivoTamanhoBytes(),
                plats,
                sis,
                online,
                pico,
                scorePopularidade);
    }

    private Double safeZscore(String gameId) {
        try {
            return redisTemplate.opsForZSet().score(RANKING_GAMES_ZSET, gameId);
        } catch (DataAccessException e) {
            return null;
        }
    }

    private static List<String> coalesceLista(List<String> l) {
        return l != null ? l : List.of();
    }

    private static String str(String s) {
        return s != null ? s : "";
    }

    private String nomeOuId(Jogo a) {
        if (a.getNome() != null && !a.getNome().isBlank()) {
            return a.getNome().toLowerCase();
        }
        return Objects.toString(a.getId(), "");
    }

    private static int clampLimiteRanking(Integer limite) {
        if (limite == null || limite <= 0) {
            return PADRAIO_LIMITE_RANKING;
        }
        return Math.min(limite, 200);
    }
}
