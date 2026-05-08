package com.projetoDados.HUB.Backend.Redis.DTO;

import java.util.List;

/**
 * Metadados (Mongo / catálogo) + métricas dinâmicas do Redis (ZSET e hashes por jogo).
 */
public record JogoRedisPublicoResponse(
        Integer posicao,
        String id,
        String nome,
        String descricao,
        String preco,
        String modoJogo,
        String status,
        boolean capaDisponivel,
        long arquivoTamanhoBytes,
        List<String> plataformasPublicacao,
        List<String> sistemasOperacionais,
        long jogadoresOnline,
        long picoJogadoresOnline,
        double scorePopularidade) {
}
