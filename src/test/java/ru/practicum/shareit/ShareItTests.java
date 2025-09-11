package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Базовый санити-чек контекста на H2.
 */
@SpringBootTest
@ActiveProfiles("test")
class ShareItTests {

	@Test
	void contextLoads() {
		// контекст поднялся — значит ОК
	}
}
