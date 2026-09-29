package vmware.services.organization;

import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableMongoRepositories
public class OrganizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrganizationApplication.class, args);
	}

	@Bean
	public OpenAPI organizationOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Organization API")
				.version("1.0")
				.description("Documentation Organization API v1.0"));
	}

	@Bean
	MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(MeterRegistry meterRegistry){
		return registry -> {
			meterRegistry.config()
					.commonTags("application", "organization");
		};
	}
}
