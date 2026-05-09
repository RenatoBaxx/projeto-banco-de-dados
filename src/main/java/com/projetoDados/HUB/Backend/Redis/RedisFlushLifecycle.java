package com.projetoDados.HUB.Backend.Redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Limpa o Redis no boot (antes do {@link CatalogoRedisWarmupRunner}) e opcionalmente no shutdown.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RedisFlushLifecycle implements ApplicationRunner {

    private final RedisConnectionFactory redisConnectionFactory;

    @Value("${app.redis.flush-on-startup:true}")
    private boolean flushOnStartup;

    @Value("${app.redis.flush-on-shutdown:false}")
    private boolean flushOnShutdown;

    @Override
    public void run(ApplicationArguments args) {
        if (!flushOnStartup) {
            return;
        }
        flushAllOuLog("startup");
    }

    @PreDestroy
    public void noShutdown() {
        if (!flushOnShutdown) {
            return;
        }
        flushAllOuLog("shutdown");
    }

    private void flushAllOuLog(String momento) {
        try (RedisConnection c = redisConnectionFactory.getConnection()) {
            c.serverCommands().flushAll();
            log.info("Redis FLUSHALL concluido ({}).", momento);
        } catch (DataAccessException e) {
            log.warn("Redis FLUSHALL ignorado em {} (Redis indisponivel?): {}", momento, e.getMessage());
        }
    }
}
