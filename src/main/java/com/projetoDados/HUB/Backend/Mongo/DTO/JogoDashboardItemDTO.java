package com.projetoDados.HUB.Backend.Mongo.DTO;

/**
 * Linha da tabela "Jogos Publicados" no Dashboard (gameId, loja, status).
 * Nomes dos componentes alinham o JSON gerado pelo Spring (camelCase).
 */
public record JogoDashboardItemDTO(String gameId, String nome, String loja, String status) {
}
