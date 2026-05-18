package com.projetoDados.HUB.Backend.Mongo.Model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Documento de jogo/arquivo no catálogo: metadados + referência ao .zip em disco.
 * Coleção MongoDB: {@code jogos}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "arquivos")
public class Jogo {

    @Id
    private String id;

    private String empresaId;

    private String nome;

    private String descricao;

    private String preco;

    private List<String> sistemasOperacionais;

    private String modoJogo;

    private List<String> plataformasPublicacao;

    private String arquivoNome;

    private long arquivoTamanhoBytes;

    /** Caminho absoluto normalizado do .zip gravado no servidor. */
    private String arquivoCaminhoRelativo;

    /** Ex.: PENDENTE, CONCLUIDO */
    private String status;

    /** Nome original do arquivo de capa (banner). */
    private String imagemNome;

    /** MIME da capa, ex.: image/jpeg */
    private String imagemContentType;

    /** Bytes da imagem armazenados no MongoDB ( BSON ). */
    @JsonIgnore
    private byte[] imagemDados;
}
