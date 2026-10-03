package vmware.services.employee.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vmware.services.employee.model.Employee;
import vmware.services.employee.repository.EmployeeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    EmployeeRepository repository;

    @InjectMocks
    EmployeeController controller;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static Employee employee(String id, Long orgId, Long deptId, String name) {
        Employee e = new Employee(orgId, deptId, name, 30, "engineer");
        e.setId(id);
        return e;
    }

    @Test
    void addPersistsTheEmployeeAndReturnsTheSavedOne() throws Exception {
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\",\"name\":\"Smith\",\"age\":25,\"position\":\"engineer\","
                                + "\"departmentId\":2,\"organizationId\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Smith"))
                .andExpect(jsonPath("$.departmentId").value(2))
                .andExpect(jsonPath("$.organizationId").value(3));

        ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Smith");
        assertThat(saved.getValue().getAge()).isEqualTo(25);
    }

    @Test
    void addRejectsMalformedJson() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void findByIdReturnsTheEmployee() throws Exception {
        when(repository.findById("1")).thenReturn(Optional.of(employee("1", 3L, 2L, "Smith")));

        mvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smith"));
    }

    @Test
    void findByIdOfUnknownEmployeeReturns404() throws Exception {
        when(repository.findById("nope")).thenReturn(Optional.empty());

        mvc.perform(get("/nope")).andExpect(status().isNotFound());
    }

    @Test
    void findAllReturnsEveryEmployeeAsJson() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
                employee("1", 1L, 1L, "Smith"), employee("2", 1L, 1L, "Johns")));

        mvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Smith"))
                .andExpect(jsonPath("$[1].name").value("Johns"));
    }

    @Test
    void findByDepartmentQueriesTheRepositoryWithTheNumericId() throws Exception {
        when(repository.findByDepartmentId(5L)).thenReturn(List.of(employee("1", 1L, 5L, "Smith")));

        mvc.perform(get("/department/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].departmentId").value(5));
    }

    @Test
    void findByOrganizationQueriesTheRepositoryWithTheNumericId() throws Exception {
        when(repository.findByOrganizationId(7L)).thenReturn(List.of(
                employee("1", 7L, 1L, "Smith"), employee("2", 7L, 2L, "Johns")));

        mvc.perform(get("/organization/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findByDepartmentRejectsNonNumericIds() throws Exception {
        mvc.perform(get("/department/abc")).andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void findByOrganizationWithNoEmployeesReturnsEmptyArray() throws Exception {
        when(repository.findByOrganizationId(99L)).thenReturn(List.of());

        mvc.perform(get("/organization/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
