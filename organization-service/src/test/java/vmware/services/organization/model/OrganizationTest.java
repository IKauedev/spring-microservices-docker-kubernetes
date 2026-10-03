package vmware.services.organization.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationTest {

    @Test
    void constructorSetsNameAndAddress() {
        Organization organization = new Organization("Acme", "Main Street");

        assertThat(organization.getId()).isNull();
        assertThat(organization.getName()).isEqualTo("Acme");
        assertThat(organization.getAddress()).isEqualTo("Main Street");
    }

    @Test
    void startsWithEmptyDepartmentAndEmployeeLists() {
        Organization organization = new Organization();

        assertThat(organization.getDepartments()).isNotNull().isEmpty();
        assertThat(organization.getEmployees()).isNotNull().isEmpty();
    }

    @Test
    void settersUpdateTheFields() {
        Organization organization = new Organization();
        organization.setId("o1");
        organization.setName("Globex");
        organization.setAddress("Second Street");
        organization.setDepartments(List.of(new Department("RD")));
        organization.setEmployees(List.of(new Employee("Smith", 25, "engineer")));

        assertThat(organization.getId()).isEqualTo("o1");
        assertThat(organization.getName()).isEqualTo("Globex");
        assertThat(organization.getAddress()).isEqualTo("Second Street");
        assertThat(organization.getDepartments()).hasSize(1);
        assertThat(organization.getEmployees()).hasSize(1);
        assertThat(organization.toString()).contains("o1", "Globex");
    }

    @Test
    void departmentHoldsItsEmployees() {
        Department department = new Department("RD");
        department.setId(1L);
        department.setEmployees(List.of(new Employee("Smith", 25, "engineer")));

        assertThat(department.getId()).isEqualTo(1L);
        assertThat(department.getName()).isEqualTo("RD");
        assertThat(department.getEmployees()).extracting(Employee::getName).containsExactly("Smith");
        assertThat(department.toString()).contains("RD");
    }

    @Test
    void employeeKeepsItsFields() {
        Employee employee = new Employee("Johns", 45, "manager");
        employee.setId(2L);

        assertThat(employee.getId()).isEqualTo(2L);
        assertThat(employee.getName()).isEqualTo("Johns");
        assertThat(employee.getAge()).isEqualTo(45);
        assertThat(employee.getPosition()).isEqualTo("manager");
        assertThat(employee.toString()).contains("Johns");
    }
}
