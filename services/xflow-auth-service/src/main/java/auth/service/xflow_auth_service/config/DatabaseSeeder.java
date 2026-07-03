package auth.service.xflow_auth_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import net.datafaker.Faker;
import lombok.RequiredArgsConstructor;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.models.enums.UserRole;
import auth.service.xflow_auth_service.repositories.UserRepository;

import java.time.OffsetDateTime;
import java.util.Locale;

@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    final UserRepository userRepository;
    final PasswordEncoder encoder;
    final Faker faker = new Faker(new Locale("en-US"));

    void generateFakeUsers(int count) {
        if (userRepository.count() > count) {
            return;
        }
        Faker faker = new Faker(new Locale("en-US"));
        for (int i = 0; i < 50; i++) {
            User user = new User();
            user.setEmail(faker.internet().emailAddress());
            user.setPassword(encoder.encode(faker.internet().password()));
            user.setRole(faker.options().option(UserRole.ROLE_OPERATOR, UserRole.ROLE_ADMIN));
            user.setActive(faker.options().option(true, false));
            userRepository.save(user);
        }
        System.out.println("✅ 50 utilisateurs aléatoires injectés avec succès !");
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {  
            System.out.println("🌱 Génération automatique des fixtures avec Datafaker...");
            generateFakeUsers(50);
        };
    }
}