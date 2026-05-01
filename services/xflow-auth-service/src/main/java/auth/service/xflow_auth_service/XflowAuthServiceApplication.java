package auth.service.xflow_auth_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.models.enums.UserRole;
import auth.service.xflow_auth_service.repositories.UserRepository;

import java.time.OffsetDateTime;

@SpringBootApplication
@ConfigurationPropertiesScan
public class XflowAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(XflowAuthServiceApplication.class, args);
	}
	
	@Bean
	CommandLineRunner initDatabase(UserRepository repository, PasswordEncoder encoder) {
		return args -> {
			System.out.println("🚀 XFlow Auth Service launched !!!");
			if (repository.findByEmail("admin@xflow.io").isEmpty()) {
				User testUser = new User();
				testUser.setEmail("admin@xflow.io");
				testUser.setPassword(encoder.encode("password123"));
				testUser.setRole(UserRole.ROLE_ADMIN);
				repository.save(testUser);
			}
			if (repository.findByEmail("operator@xflow.io").isEmpty()) {
				User testUser = new User();
				testUser.setEmail("operator@xflow.io");
				testUser.setPassword(encoder.encode("password123"));
				testUser.setPin(encoder.encode("123456"));
				testUser.setRole(UserRole.ROLE_OPERATOR);
				repository.save(testUser);
			}
		};
	}
}
