package com.projetoDados.HUB.Backend.Redis.DTO;

import java.util.List;

/**
 * Item de ranking servido ao frontend (campos alinhados ao {@link com.projetoDados.HUB.Backend.Mongo.Model.ArquivoDocumento}).
 */
public record RankingJogoItemResponse(
        int posicao,
        String id,
        String nome,
        String descricao,
        String preco,
        String modoJogo,
        String status,
        String arquivoNome,
        long arquivoTamanhoBytes,
        List<String> sistemasOperacionais,
        List<String> plataformasPublicacao) {
}
