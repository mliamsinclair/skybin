package proj.skybin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SkybinApplicationTests {

	@Test
	void contextLoads() {
		assertNotNull("The context should have loaded successfully");
	}

}
