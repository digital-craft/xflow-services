package map.service.xflow_map_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import map.service.xflow_map_service.models.MapVersion;
import map.service.xflow_map_service.repositories.MapVersionRepository;

import java.util.UUID;

@SpringBootApplication
public class XflowMapServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(XflowMapServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(MapVersionRepository repository) {
		return args -> {
			System.out.println("🚀 XFlow Map Service launched !!!");
			if (repository.findByTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000")).isEmpty()) {
				MapVersion mapVersion = new MapVersion();
				mapVersion.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
				mapVersion.setSequenceNumber(1);
				repository.save(mapVersion);
			}
		};
	}

}
