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

    public void playerEnter(String gameId, String userId) {

        String statsKey = "game:" + gameId + ":stats";
        String usersKey = "game:" + gameId + ":users";

        // adiciona usuário
        redisTemplate.opsForSet().add(usersKey, userId);

        // pega online atual
        Object onlineObj = redisTemplate.opsForHash().get(statsKey, "online");
        Long online = onlineObj != null ? Long.parseLong(onlineObj.toString()) : 0L;

        // incrementa
        online++;

        // salva online
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
        return redisTemplate.opsForZSet().reverseRange("ranking:games", 0, 9);
    }
}