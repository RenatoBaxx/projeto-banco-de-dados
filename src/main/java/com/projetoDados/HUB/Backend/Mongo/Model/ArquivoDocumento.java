package com.projetoDados.HUB.Backend.Mongo.Model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Documento de jogo/arquivo no catálogo: metadados + referência ao .zip em disco.
 * Coleção mantém o nome {@code arquivos} (legado).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "arquivos")
public class ArquivoDocumento {

    @Id
    private String id;

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
}
