package com.projetoDados.HUB;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Ponto de entrada: liga Web MVC, Mongo, Redis, agendamento e varredura dos repositórios Mongo. */
@SpringBootApplication
@EnableScheduling
@EnableMongoRepositories(basePackages = "com.projetoDados.HUB.Backend.Mongo.Repository")
public class HubApplication {

	public static void main(String[] args) {
		SpringApplication.run(HubApplication.class, args);
	}

}
