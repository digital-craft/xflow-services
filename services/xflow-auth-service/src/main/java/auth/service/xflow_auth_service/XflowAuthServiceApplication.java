package auth.service.xflow_auth_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XflowAuthServiceApplication implements CommandLineRunner  {

	public static void main(String[] args) {
		SpringApplication.run(XflowAuthServiceApplication.class, args);
	}

	@Override
    public void run(String... args) throws Exception {
        System.out.println("Auth service launched !!");
    }
}
