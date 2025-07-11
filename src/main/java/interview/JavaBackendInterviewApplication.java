package interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JavaBackendInterviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaBackendInterviewApplication.class, args);
	}

}
