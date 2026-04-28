package com.projetoDados.HUB.Backend.Redis.Service;

import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.projetoDados.HUB.Backend.Redis.Model.GameStats;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameStatsService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ---------- MÉTRICAS (HASH) ----------

    public void playerEnter(String gameId, String userId) {

        String statsKey = "game:" + gameId + ":stats";
        String usersKey = "game:" + gameId + ":users";

        // adiciona usuário
        redisTemplate.opsForSet().add(usersKey, userId);

        // incrementa online
        Long online = redisTemplate.opsForValue()
                .increment("game:" + gameId + ":online");

        // 🔥 CRIA/ATUALIZA HASH
        redisTemplate.opsForHash().put(statsKey, "online", online.toString());

        // max
        Object maxObj = redisTemplate.opsForHash().get(statsKey, "max");
        Long max = maxObj != null ? Long.parseLong(maxObj.toString()) : 0L;

        if (online > max) {
            redisTemplate.opsForHash().put(statsKey, "max", online.toString());
        }

        // min (primeira vez)
        Object minObj = redisTemplate.opsForHash().get(statsKey, "min");
        if (minObj == null) {
            redisTemplate.opsForHash().put(statsKey, "min", online.toString());
        }

        // ranking
        redisTemplate.opsForZSet().incrementScore("ranking:games", gameId, 1);
    }

    public void playerLeave(String gameId, String userId) {

        String statsKey = "game:" + gameId + ":stats";
        String usersKey = "game:" + gameId + ":users";

        redisTemplate.opsForSet().remove(usersKey, userId);

        Long online = redisTemplate.opsForValue()
                .decrement("game:" + gameId + ":online");

        redisTemplate.opsForHash().put(statsKey, "online", online);
    }

    public GameStats getStats(String gameId) {

        String statsKey = "game:" + gameId + ":stats";

        Map<Object, Object> data = redisTemplate.opsForHash().entries(statsKey);

        if (data.isEmpty()) return null;

        return GameStats.builder()
                .gameId(gameId)
                .online(Long.parseLong(data.get("online").toString()))
                .max(Long.parseLong(data.get("max").toString()))
                .min(Long.parseLong(data.get("min").toString()))
                .build();
    }

    // ---------- USUÁRIOS ONLINE ----------

    public Set<Object> getOnlineUsers(String gameId) {
        return redisTemplate.opsForSet().members("game:" + gameId + ":users");
    }

    // ---------- RANKING ----------

    public Set<Object> getTopGames() {
        return redisTemplate.opsForZSet()
                .reverseRange("ranking:games", 0, 9);
    }
}