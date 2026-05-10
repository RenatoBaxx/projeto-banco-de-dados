package com.projetoDados.HUB.Backend.Redis;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.projetoDados.HUB.Backend.Redis.Service.CatalogoJogoRankingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Depois do flush (se houver): copia todos os jogos do Mongo para as chaves {@code catalog:*} no Redis,
 * com ordem da lista embaralhada uma vez por subida.
 * <p>
 * <b>O que entrega:</b> cache de catálogo pronto para {@code CatalogoJogoRankingService.listarRanking()}
 * e hashes atualizados quando o {@code ApplicationRunner} termina sem erro.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class CatalogoRedisWarmupRunner implements ApplicationRunner {

    private final CatalogoJogoRankingService catalogoJogoRankingService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            catalogoJogoRankingService.recarregarDoMongoParaRedis();
        } catch (Exception e) {
            log.error(
                    "Falha ao popular Redis com o catalogo Mongo (verifique REDIS_HOST/porta). App segue sem cache de ranking.",
                    e);
        }
    }
}
