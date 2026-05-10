package com.projetoDados.HUB.Backend.Redis;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints só para desenvolvimento: confere tipos Redis básicos. Ativo com {@code --spring.profiles.active=dev}.
 */
@Profile("dev")
@RestController
public class RedisTestController {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTestController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis-test")
    public String testRedis() {

        redisTemplate.opsForValue().set("test:redis", "funcionando!");
        Object valor = redisTemplate.opsForValue().get("test:redis");

        redisTemplate.opsForValue().increment("test:contador");

        redisTemplate.opsForHash().put("test:hash", "campo", "valor");

        redisTemplate.opsForSet().add("test:set", "user1");

        return "Redis OK -> valor: " + valor;
    }
}
