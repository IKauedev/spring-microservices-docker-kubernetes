package vmware.services.employee;

import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import vmware.services.employee.repository.EmployeeRepository;

@SpringBootApplication
@EnableDiscoveryClient
@EnableMongoRepositories
public class EmployeeApplication {

	@Autowired
    EmployeeRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(EmployeeApplication.class, args);
	}

	@Bean
	public OpenAPI employeeOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Employee API")
				.version("1.0")
				.description("Documentation Employee API v1.0"));
	}

	@Bean
	MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(MeterRegistry meterRegistry){
		return registry -> {
			meterRegistry.config()
					.commonTags("application", "employee");
		};
	}
}
