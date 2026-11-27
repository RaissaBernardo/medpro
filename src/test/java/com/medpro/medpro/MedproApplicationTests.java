package com.medpro.medpro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "api.security.token.secret=chave-exclusiva-para-testes-locais")
class MedproApplicationTests {

	@Test
	void contextLoads() {
	}

}
