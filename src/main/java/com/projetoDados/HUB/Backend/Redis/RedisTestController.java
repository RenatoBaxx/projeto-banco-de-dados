package com.projetoDados.HUB.Backend.Redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/redis-test")
    public String testRedis() {
        // salva um valor
        redisTemplate.opsForValue().set("chave", "funcionando!");
        // recupera o valor
        String valor = redisTemplate.opsForValue().get("chave");
        return "Redis respondeu: " + valor;
    }
}
