package com.projetoDados.HUB.Backend.Redis.Schedule;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.projetoDados.HUB.Backend.Mongo.Model.ArquivoDocumento;
import com.projetoDados.HUB.Backend.Mongo.Repository.ArquivoDocumentoRepository;
import com.projetoDados.HUB.Backend.Redis.Service.GameStatsService;

import lombok.extern.slf4j.Slf4j;

/**
 * Simula jogadores online no Redis: valor inicial aleatório por jogo no boot e oscilação periódica.
 */
@Slf4j
@Component
@ConditionalOnProperty(
        name = "app.simulacao-jogadores-online.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class SimulacaoJogadoresOnlineScheduler {

    private static final long ONLINE_MIN_INCLUSIVO = 100L;
    private static final long ONLINE_MAX_EXCLUSIVO = 10_001L;

    private final ArquivoDocumentoRepository arquivoDocumentoRepository;
    private final GameStatsService gameStatsService;
    private final int variacaoMaximaAbsoluta;

    public SimulacaoJogadoresOnlineScheduler(
            ArquivoDocumentoRepository arquivoDocumentoRepository,
            GameStatsService gameStatsService,
            @Value("${app.simulacao-jogadores-online.variacao-max-absoluta:500}") int variacaoMaximaAbsoluta) {
        this.arquivoDocumentoRepository = arquivoDocumentoRepository;
        this.gameStatsService = gameStatsService;
        this.variacaoMaximaAbsoluta = variacaoMaximaAbsoluta;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void semearContagemInicial() {
        Iterable<ArquivoDocumento> todos = arquivoDocumentoRepository.findAll();
        int n = 0;
        for (ArquivoDocumento doc : todos) {
            long online = ThreadLocalRandom.current().nextLong(ONLINE_MIN_INCLUSIVO, ONLINE_MAX_EXCLUSIVO);
            gameStatsService.definirBaselineSimulacao(doc.getId(), online);
            n++;
        }
        log.info("Simulacao jogadores online: {} jogo(s) com contagem inicial aleatoria [{}..{}].", n,
                ONLINE_MIN_INCLUSIVO, ONLINE_MAX_EXCLUSIVO - 1);
    }

    @Scheduled(fixedRateString = "${app.simulacao-jogadores-online.interval-ms:10000}")
    public void oscilarOnline() {
        int limite = Math.max(0, variacaoMaximaAbsoluta);
        for (ArquivoDocumento doc : arquivoDocumentoRepository.findAll()) {
            gameStatsService.aplicarVariacaoAleatoriaOnline(doc.getId(), limite);
        }
    }
}
