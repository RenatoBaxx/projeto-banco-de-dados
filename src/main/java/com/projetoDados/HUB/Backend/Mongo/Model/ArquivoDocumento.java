package com.projetoDados.HUB.Backend.Mongo.Model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "arquivos")
public class ArquivoDocumento {

    @Id
    private String id;

    private String arquivo;

    private String nome;

    private String descricao;

    private List<String> categorias;

    private List<String> requisitos;
}

