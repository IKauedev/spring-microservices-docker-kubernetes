package vmware.services.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import vmware.services.employee.model.Employee;
import vmware.services.employee.repository.EmployeeRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Sobe a aplicacao inteira (Tomcat real, Jackson, springdoc, actuator) sem Kubernetes e sem MongoDB:
 * o repositorio e substituido por um mock.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.cloud.kubernetes.enabled=false")
class EmployeeApplicationTests {

    @Value("${local.server.port}")
    int port;

    @MockitoBean
    EmployeeRepository repository;

    private HttpResponse<String> get(String path) throws Exception {
        return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void listsEmployeesOverHttp() throws Exception {
        Employee smith = new Employee(1L, 1L, "Smith", 25, "engineer");
        smith.setId("1");
        when(repository.findAll()).thenReturn(List.of(smith));

        HttpResponse<String> response = get("/");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"name\":\"Smith\"");
    }

    @Test
    void publishesTheOpenApiDocument() throws Exception {
        HttpResponse<String> response = get("/v3/api-docs");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Employee API");
    }
}
