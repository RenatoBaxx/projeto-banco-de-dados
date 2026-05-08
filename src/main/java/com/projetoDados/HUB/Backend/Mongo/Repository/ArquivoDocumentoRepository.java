package com.projetoDados.HUB.Backend.Mongo.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.projetoDados.HUB.Backend.Mongo.Model.ArquivoDocumento;

public interface ArquivoDocumentoRepository extends MongoRepository<ArquivoDocumento, String> {
}

