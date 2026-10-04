package vmware.services.employee.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vmware.services.employee.exception.GlobalExceptionHandler;
import vmware.services.employee.model.Employee;
import vmware.services.employee.repository.EmployeeRepository;
import vmware.services.employee.service.EmployeeService;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    EmployeeRepository repository;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        EmployeeController controller = new EmployeeController(new EmployeeService(repository));
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler()).build();
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

    @Test
    void addRejectsABlankName() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"age\":25}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void addRejectsAnImpossibleAge() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Smith\",\"age\":-1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void updateReplacesTheEmployeeKeepingTheIdFromTheUrl() throws Exception {
        when(repository.existsById("1")).thenReturn(true);
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(put("/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"other\",\"name\":\"Johns\",\"age\":40,\"position\":\"manager\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Johns"));
    }

    @Test
    void updateOfUnknownEmployeeReturns404WithoutSaving() throws Exception {
        when(repository.existsById("nope")).thenReturn(false);

        mvc.perform(put("/nope").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Johns\",\"age\":40}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Employee not found: nope"));

        verify(repository, never()).save(any(Employee.class));
    }

    @Test
    void deleteRemovesTheEmployeeAndReturns204() throws Exception {
        when(repository.existsById("1")).thenReturn(true);

        mvc.perform(delete("/1")).andExpect(status().isNoContent());

        verify(repository).deleteById("1");
    }

    @Test
    void deleteOfUnknownEmployeeReturns404() throws Exception {
        when(repository.existsById("nope")).thenReturn(false);

        mvc.perform(delete("/nope")).andExpect(status().isNotFound());

        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteByDepartmentReportsHowManyWereRemoved() throws Exception {
        List<Employee> employees = List.of(employee("1", 1L, 5L, "Smith"), employee("2", 1L, 5L, "Johns"));
        when(repository.findByDepartmentId(5L)).thenReturn(employees);

        mvc.perform(delete("/department/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(2));

        verify(repository).deleteAll(employees);
    }

    @Test
    void searchWithoutFiltersPagesThroughEveryEmployee() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("name"));
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(employee("1", 1L, 1L, "Smith")), pageable, 1));

        mvc.perform(get("/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void searchCombinesNameAndPositionAndCapsThePageSize() throws Exception {
        Pageable pageable = PageRequest.of(2, 100, org.springframework.data.domain.Sort.by("name"));
        when(repository.findByNameContainingIgnoreCaseAndPositionIgnoreCase("smi", "engineer", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mvc.perform(get("/search").param("name", "smi").param("position", "engineer")
                        .param("page", "2").param("size", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void countReturnsTheTotal() throws Exception {
        when(repository.count()).thenReturn(42L);

        mvc.perform(get("/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(42));
    }

    @Test
    void countByDepartmentAndOrganization() throws Exception {
        when(repository.countByDepartmentId(5L)).thenReturn(3L);
        when(repository.countByOrganizationId(7L)).thenReturn(9L);

        mvc.perform(get("/department/5/count")).andExpect(jsonPath("$.count").value(3));
        mvc.perform(get("/organization/7/count")).andExpect(jsonPath("$.count").value(9));
    }

    @Test
    void statsSummarisesAgeAndPositions() throws Exception {
        Employee a = employee("1", 1L, 1L, "A");
        a.setAge(20);
        Employee b = employee("2", 1L, 1L, "B");
        b.setAge(40);
        b.setPosition("manager");
        when(repository.findAll()).thenReturn(List.of(a, b));

        mvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.averageAge").value(30.0))
                .andExpect(jsonPath("$.byPosition.engineer").value(1))
                .andExpect(jsonPath("$.byPosition.manager").value(1));
    }

    @Test
    void statsOfAnEmptyCollectionIsZero() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mvc.perform(get("/stats"))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.averageAge").value(0.0));
    }
}
