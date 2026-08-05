package com.campusride.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.jwt.secret=Y2FtcHVzcmlkZS1hdXRoLXNlcnZpY2UtdGVzdC1zZWNyZXQta2V5LTIwMjY=")
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
