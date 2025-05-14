package com.nametrek.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "MAIL_HOST=localhost",
    "MAIL_PORT=1025",
    "REDIS_HOST=localhost",
    "REDIS_PORT=6379",
    "REDIS_PASSWORD=********",
    "REDIS_USERNAME=default",
    "PGHOST=localhost",
    "PGDATABASE=nametrek_test",
    "PGUSER=default",
    "PGPASSWORD=********",
    "GMAIL_USERNAME=say@example.com",
    "GMAIL_PASSWORD=********"
})
@ActiveProfiles("test")
class NametrekApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
