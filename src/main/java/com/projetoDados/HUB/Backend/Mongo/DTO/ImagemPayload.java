package com.projetoDados.HUB.Backend.Mongo.DTO;

/**
 * Bytes da capa e Content-Type para resposta HTTP.
 */
public record ImagemPayload(byte[] dados, String contentType) {
}
