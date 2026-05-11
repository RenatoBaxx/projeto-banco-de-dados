package com.projetoDados.HUB.Backend.Mongo.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.projetoDados.HUB.Backend.Mongo.Model.Jogo;

public interface JogoRepository extends MongoRepository<Jogo, String> {
}
