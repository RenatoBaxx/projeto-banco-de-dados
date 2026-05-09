package com.projetoDados.HUB;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.simulacao-jogadores-online.enabled=false")
class HubApplicationTests {

	@Test
	void contextLoads() {
	}

}
