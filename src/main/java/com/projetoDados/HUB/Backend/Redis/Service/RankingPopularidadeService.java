package com.projetoDados.HUB.Backend.Redis.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.projetoDados.HUB.Backend.Mongo.Model.ArquivoDocumento;
import com.projetoDados.HUB.Backend.Mongo.Repository.ArquivoDocumentoRepository;
import com.projetoDados.HUB.Backend.Redis.DTO.JogoRedisPublicoResponse;
import com.projetoDados.HUB.Backend.Redis.Model.GameStats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Ranking e catálogo enriquecidos: ZSET {@code ranking:games} (popularidade rápida) + hashes
 * {@code game:{id}:stats} (online / pico) + documento Mongo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingPopularidadeService {

    /** Limite máximo quando o cliente não informa tamanho. */
    private static final int PADRAIO_LIMITE = 80;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArquivoDocumentoRepository arquivoDocumentoRepository;
    private final GameStatsService gameStatsService;

    /** Ordem por score Redis; se não houver ZSET ainda, cai para lista ordenada por nome só com métricas. */
    public List<JogoRedisPublicoResponse> rankingPorAtividade(Integer limite) {
        int n = clampLimite(limite);
        try {
            Set<TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(GameStatsService.RANKING_GAMES_ZSET, 0, n - 1);
            if (tuples != null && !tuples.isEmpty()) {
                return montarRankingDesdeZset(tuples, n);
            }
        } catch (DataAccessException e) {
            log.warn("Ranking Redis ZSET indisponivel: {}", e.getMessage());
        }
        return fallbackCatalogoOrdenado(n);
    }

    /** Catálogo completo (Mongo) com métricas ao vivo por jogo — para listagem exploratória. */
    public List<JogoRedisPublicoResponse> catalogoComMetricas() {
        List<ArquivoDocumento> docs = arquivoDocumentoRepository.findAll().stream()
                .sorted(Comparator.comparing(a -> nomeOrId(a)))
                .collect(Collectors.toList());
        List<JogoRedisPublicoResponse> lista = new ArrayList<>();
        for (ArquivoDocumento d : docs) {
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
            Optional<ArquivoDocumento> doc = arquivoDocumentoRepository.findById(id);
            GameStats st = gameStatsService.getStats(id);
            resultado.add(montarLinhaCompleta(posicao++, id, doc.orElse(null), st, score));
        }
        return resultado;
    }

    private List<JogoRedisPublicoResponse> fallbackCatalogoOrdenado(int limite) {
        List<ArquivoDocumento> docs = arquivoDocumentoRepository.findAll().stream()
                .sorted(Comparator.comparing(a -> nomeOrId(a)))
                .limit(limite)
                .collect(Collectors.toList());
        List<JogoRedisPublicoResponse> lista = new ArrayList<>();
        int p = 1;
        for (ArquivoDocumento d : docs) {
            Double z = safeZscore(d.getId());
            double score = z != null ? z : 0.0;
            GameStats st = gameStatsService.getStats(d.getId());
            lista.add(montarLinhaCompleta(p++, d.getId(), d, st, score));
        }
        return lista;
    }

    private JogoRedisPublicoResponse montarLinhaCatalogoSemPosicao(ArquivoDocumento d) {
        GameStats st = gameStatsService.getStats(d.getId());
        Double z = safeZscore(d.getId());
        double score = z != null ? z : 0.0;
        return montarLinhaCompleta(null, d.getId(), d, st, score);
    }

    private JogoRedisPublicoResponse montarLinhaCompleta(
            Integer posicao,
            String id,
            ArquivoDocumento d,
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
            return redisTemplate.opsForZSet().score(GameStatsService.RANKING_GAMES_ZSET, gameId);
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

    private static String nomeOrId(ArquivoDocumento a) {
        if (a.getNome() != null && !a.getNome().isBlank()) {
            return a.getNome().toLowerCase();
        }
        return Objects.toString(a.getId(), "");
    }

    private static int clampLimite(Integer limite) {
        if (limite == null || limite <= 0) {
            return PADRAIO_LIMITE;
        }
        return Math.min(limite, 200);
    }
}
