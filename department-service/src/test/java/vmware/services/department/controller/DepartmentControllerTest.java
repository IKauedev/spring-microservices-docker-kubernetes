package vmware.services.department.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vmware.services.department.client.EmployeeClient;
import vmware.services.department.model.Department;
import vmware.services.department.model.Employee;
import vmware.services.department.repository.DepartmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    @Mock
    DepartmentRepository repository;

    @Mock
    EmployeeClient employeeClient;

    @InjectMocks
    DepartmentController controller;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static Department department(String id, Long organizationId, String name) {
        Department d = new Department(organizationId, name);
        d.setId(id);
        return d;
    }

    @Test
    void addPersistsTheDepartment() throws Exception {
        when(repository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\",\"name\":\"RD Dept.\",\"organizationId\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("RD Dept."))
                .andExpect(jsonPath("$.organizationId").value(4));

        verify(repository).save(any(Department.class));
    }

    @Test
    void addRejectsMalformedJson() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void findByIdReturnsTheDepartment() throws Exception {
        when(repository.findById("1")).thenReturn(Optional.of(department("1", 4L, "RD Dept.")));

        mvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("RD Dept."));
    }

    @Test
    void findByIdOfUnknownDepartmentReturns404() throws Exception {
        when(repository.findById("nope")).thenReturn(Optional.empty());

        mvc.perform(get("/nope")).andExpect(status().isNotFound());
    }

    @Test
    void findAllListsEveryDepartment() throws Exception {
        when(repository.findAll()).thenReturn(List.of(department("1", 1L, "A"), department("2", 1L, "B")));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    @Test
    void findByOrganizationFiltersByNumericOrganizationId() throws Exception {
        when(repository.findByOrganizationId(4L)).thenReturn(List.of(department("1", 4L, "A")));

        mvc.perform(get("/organization/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].organizationId").value(4));
    }

    @Test
    void findByOrganizationRejectsNonNumericIds() throws Exception {
        mvc.perform(get("/organization/abc")).andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void withEmployeesAsksTheEmployeeServiceForEachDepartment() throws Exception {
        when(repository.findByOrganizationId(4L)).thenReturn(new ArrayList<>(List.of(
                department("d1", 4L, "A"), department("d2", 4L, "B"))));
        when(employeeClient.findByDepartment("d1")).thenReturn(List.of(new Employee("Smith", 25, "engineer")));
        when(employeeClient.findByDepartment("d2")).thenReturn(List.of());

        mvc.perform(get("/organization/4/with-employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employees.length()").value(1))
                .andExpect(jsonPath("$[0].employees[0].name").value("Smith"))
                .andExpect(jsonPath("$[1].employees.length()").value(0));

        verify(employeeClient).findByDepartment("d1");
        verify(employeeClient).findByDepartment("d2");
    }

    @Test
    void withEmployeesDoesNotCallTheEmployeeServiceWhenThereAreNoDepartments() throws Exception {
        when(repository.findByOrganizationId(9L)).thenReturn(new ArrayList<>());

        mvc.perform(get("/organization/9/with-employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verifyNoInteractions(employeeClient);
    }

    @Test
    void feignEndpointListsTheEmployeesOfDepartmentOne() throws Exception {
        when(employeeClient.findByDepartment("1")).thenReturn(List.of(new Employee("Johns", 45, "manager")));

        mvc.perform(get("/feign"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Johns"));
    }
}
