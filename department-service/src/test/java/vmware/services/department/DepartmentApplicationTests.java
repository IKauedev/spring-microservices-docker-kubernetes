package vmware.services.department;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import vmware.services.department.client.EmployeeClient;
import vmware.services.department.model.Department;
import vmware.services.department.model.Employee;
import vmware.services.department.repository.DepartmentRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Sobe a aplicacao inteira sem Kubernetes e sem MongoDB: repositorio e cliente Feign
 * (que falaria com o employee-service) sao mocks.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.cloud.kubernetes.enabled=false")
class DepartmentApplicationTests {

    @Value("${local.server.port}")
    int port;

    @MockitoBean
    DepartmentRepository repository;

    @MockitoBean
    EmployeeClient employeeClient;

    private HttpResponse<String> get(String path) throws Exception {
        return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void returnsDepartmentsWithTheirEmployeesOverHttp() throws Exception {
        Department department = new Department(4L, "RD Dept.");
        department.setId("d1");
        when(repository.findByOrganizationId(4L)).thenReturn(new ArrayList<>(List.of(department)));
        when(employeeClient.findByDepartment("d1")).thenReturn(List.of(new Employee("Smith", 25, "engineer")));

        HttpResponse<String> response = get("/organization/4/with-employees");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"name\":\"RD Dept.\"").contains("\"name\":\"Smith\"");
    }

    @Test
    void publishesTheOpenApiDocument() throws Exception {
        HttpResponse<String> response = get("/v3/api-docs");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Department API");
    }
}
