package com.projetoDados.HUB.Backend.Redis.Controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projetoDados.HUB.Backend.Redis.DTO.JogoRedisPublicoResponse;
import com.projetoDados.HUB.Backend.Redis.Model.GameStats;
import com.projetoDados.HUB.Backend.Redis.Service.GameStatsService;
import com.projetoDados.HUB.Backend.Redis.Service.RankingPopularidadeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class GameStatsController {

    private final GameStatsService gameStatsService;
    private final RankingPopularidadeService rankingPopularidadeService;

    // ---------- ROTAS ESTÁTICAS (antes de /{gameId}) ----------

    @GetMapping("/ranking")
    public ResponseEntity<Set<Object>> getRanking() {
        return ResponseEntity.ok(gameStatsService.getTopGames());
    }

    /** Ranking: ZSET popularidade + online/pico nos hashes + metadados Mongo. */
    @GetMapping("/ranking/atividade")
    public ResponseEntity<List<JogoRedisPublicoResponse>> rankingPorAtividade(
            @RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok(rankingPopularidadeService.rankingPorAtividade(limite));
    }

    /** Catálogo Mongo com métricas Redis por linha (exploração rápida). */
    @GetMapping("/catalogo/metricas")
    public ResponseEntity<List<JogoRedisPublicoResponse>> catalogoComMetricas() {
        return ResponseEntity.ok(rankingPopularidadeService.catalogoComMetricas());
    }

    // ---------- POR GAME ID ----------

    @PostMapping("/{gameId}/enter")
    public ResponseEntity<Void> enterGame(
            @PathVariable String gameId,
            @RequestParam String userId) {

        gameStatsService.playerEnter(gameId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{gameId}/leave")
    public ResponseEntity<Void> leaveGame(
            @PathVariable String gameId,
            @RequestParam String userId) {

        gameStatsService.playerLeave(gameId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStats> getStats(@PathVariable String gameId) {

        GameStats stats = gameStatsService.getStats(gameId);

        if (stats == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{gameId}/users")
    public ResponseEntity<Set<Object>> getUsers(@PathVariable String gameId) {
        return ResponseEntity.ok(gameStatsService.getOnlineUsers(gameId));
    }
}
