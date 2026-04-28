package com.projetoDados.HUB.Backend.Redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisTestController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis-test")
    public String testRedis() {

        // STRING
        redisTemplate.opsForValue().set("test:redis", "funcionando!");
        String valor = redisTemplate.opsForValue().get("test:redis");

        // INCREMENT
        redisTemplate.opsForValue().increment("test:contador");

        // HASH
        redisTemplate.opsForHash().put("test:hash", "campo", "valor");

        // SET
        redisTemplate.opsForSet().add("test:set", "user1");

        return "Redis OK -> valor: " + valor;
    }
}