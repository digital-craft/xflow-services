package auth.service.xflow_auth_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.OffsetDateTime;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class XflowAuthServiceApplication {

	public static void main(String[] args) {
		System.out.println("🚀 XFlow Auth Service launched !!!");
		SpringApplication.run(XflowAuthServiceApplication.class, args);
	}
}
