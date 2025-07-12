package interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EventApp {

	public static void main(String[] args) {
		SpringApplication.run(EventApp.class, args);
	}

}
