package vmware.services.organization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import vmware.services.organization.client.DepartmentClient;
import vmware.services.organization.client.EmployeeClient;
import vmware.services.organization.model.Employee;
import vmware.services.organization.model.Organization;
import vmware.services.organization.repository.OrganizationRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Sobe a aplicacao inteira sem Kubernetes e sem MongoDB: repositorio e clientes Feign
 * (employee-service e department-service) sao mocks.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.cloud.kubernetes.enabled=false")
class OrganizationApplicationTests {

    @Value("${local.server.port}")
    int port;

    @MockitoBean
    OrganizationRepository repository;

    @MockitoBean
    DepartmentClient departmentClient;

    @MockitoBean
    EmployeeClient employeeClient;

    private HttpResponse<String> get(String path) throws Exception {
        return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void returnsAnOrganizationWithItsEmployeesOverHttp() throws Exception {
        Organization acme = new Organization("Acme", "Main Street");
        acme.setId("1");
        when(repository.findById("1")).thenReturn(Optional.of(acme));
        when(employeeClient.findByOrganization("1")).thenReturn(List.of(new Employee("Smith", 25, "engineer")));

        HttpResponse<String> response = get("/1/with-employees");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"name\":\"Acme\"").contains("\"name\":\"Smith\"");
    }

    @Test
    void publishesTheOpenApiDocument() throws Exception {
        HttpResponse<String> response = get("/v3/api-docs");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Organization API");
    }
}
