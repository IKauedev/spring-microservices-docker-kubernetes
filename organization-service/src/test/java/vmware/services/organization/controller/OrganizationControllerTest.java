package vmware.services.organization.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vmware.services.organization.client.DepartmentClient;
import vmware.services.organization.client.EmployeeClient;
import vmware.services.organization.model.Department;
import vmware.services.organization.model.Employee;
import vmware.services.organization.model.Organization;
import vmware.services.organization.repository.OrganizationRepository;

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
class OrganizationControllerTest {

    @Mock
    OrganizationRepository repository;

    @Mock
    DepartmentClient departmentClient;

    @Mock
    EmployeeClient employeeClient;

    @InjectMocks
    OrganizationController controller;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static Organization organization(String id, String name) {
        Organization o = new Organization(name, "Main Street");
        o.setId(id);
        return o;
    }

    @Test
    void addPersistsTheOrganization() throws Exception {
        when(repository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\",\"name\":\"Acme\",\"address\":\"Main Street\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.address").value("Main Street"));

        verify(repository).save(any(Organization.class));
    }

    @Test
    void addAcceptsNestedDepartmentsAndEmployees() throws Exception {
        when(repository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\",\"name\":\"Acme\",\"departments\":[{\"id\":1,\"name\":\"RD\","
                                + "\"employees\":[{\"id\":1,\"name\":\"Smith\",\"age\":25,\"position\":\"engineer\"}]}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departments[0].name").value("RD"))
                .andExpect(jsonPath("$.departments[0].employees[0].name").value("Smith"));
    }

    @Test
    void addRejectsMalformedJson() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void findAllListsEveryOrganization() throws Exception {
        when(repository.findAll()).thenReturn(List.of(organization("1", "Acme"), organization("2", "Globex")));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].name").value("Globex"));
    }

    @Test
    void findByIdReturnsTheOrganization() throws Exception {
        when(repository.findById("1")).thenReturn(Optional.of(organization("1", "Acme")));

        mvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme"));
    }

    @Test
    void findByIdOfUnknownOrganizationReturns404() throws Exception {
        when(repository.findById("nope")).thenReturn(Optional.empty());

        mvc.perform(get("/nope")).andExpect(status().isNotFound());
    }

    @Test
    void withDepartmentsFillsTheDepartmentsFromTheDepartmentService() throws Exception {
        when(repository.findById("1")).thenReturn(Optional.of(organization("1", "Acme")));
        when(departmentClient.findByOrganization("1")).thenReturn(List.of(new Department("RD"), new Department("Sales")));

        mvc.perform(get("/1/with-departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departments.length()").value(2))
                .andExpect(jsonPath("$.departments[0].name").value("RD"));
    }

    @Test
    void withDepartmentsAndEmployeesUsesTheDedicatedFeignCall() throws Exception {
        Department rd = new Department("RD");
        rd.setEmployees(List.of(new Employee("Smith", 25, "engineer")));
        when(repository.findById("1")).thenReturn(Optional.of(organization("1", "Acme")));
        when(departmentClient.findByOrganizationWithEmployees("1")).thenReturn(List.of(rd));

        mvc.perform(get("/1/with-departments-and-employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departments[0].employees[0].name").value("Smith"));

        verify(departmentClient).findByOrganizationWithEmployees("1");
    }

    @Test
    void withEmployeesFillsTheEmployeesFromTheEmployeeService() throws Exception {
        when(repository.findById("1")).thenReturn(Optional.of(organization("1", "Acme")));
        when(employeeClient.findByOrganization("1")).thenReturn(List.of(new Employee("Johns", 45, "manager")));

        mvc.perform(get("/1/with-employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees.length()").value(1))
                .andExpect(jsonPath("$.employees[0].name").value("Johns"));
    }

    @Test
    void enrichedLookupsOfUnknownOrganizationsReturn404WithoutCallingOtherServices() throws Exception {
        when(repository.findById("404")).thenReturn(Optional.empty());

        mvc.perform(get("/404/with-departments")).andExpect(status().isNotFound());
        mvc.perform(get("/404/with-departments-and-employees")).andExpect(status().isNotFound());
        mvc.perform(get("/404/with-employees")).andExpect(status().isNotFound());

        verifyNoInteractions(departmentClient, employeeClient);
    }
}
