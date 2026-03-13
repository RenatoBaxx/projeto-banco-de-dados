package com.projetoDados.HUB.Backend.Redis.Service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.projetoDados.HUB.Backend.Redis.Model.Upload;

@Service
public class UploadService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void createUpload(Upload upload) {

        String key = "upload:" + upload.getGameId();

        redisTemplate.opsForHash().put(key, "gameId", upload.getGameId());
        redisTemplate.opsForHash().put(key, "loja", upload.getLoja());
        redisTemplate.opsForHash().put(key, "status", upload.getStatus());

        redisTemplate.opsForList().rightPush("upload:queue", upload.getGameId());
    }

    public Upload getUpload(String gameId) {

        Map<Object, Object> data = redisTemplate.opsForHash().entries("upload:" + gameId);

        if (data.isEmpty()) {
            return null;
        }

        return Upload.builder()
                .gameId((String) data.get("gameId"))
                .loja((String) data.get("loja"))
                .status((String) data.get("status"))
                .build();
    }

    public void updateUpload(String gameId, String status) {
        redisTemplate.opsForHash().put("upload:" + gameId, "status", status);
    }

    public void deleteUpload(String gameId) {
        redisTemplate.delete("upload:" + gameId);
    }
}