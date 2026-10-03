package vmware.services.department.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentTest {

    @Test
    void constructorSetsOrganizationAndName() {
        Department department = new Department(7L, "RD Dept.");

        assertThat(department.getId()).isNull();
        assertThat(department.getOrganizationId()).isEqualTo(7L);
        assertThat(department.getName()).isEqualTo("RD Dept.");
    }

    @Test
    void startsWithAnEmptyEmployeeList() {
        assertThat(new Department().getEmployees()).isNotNull().isEmpty();
    }

    @Test
    void settersUpdateTheFields() {
        Department department = new Department();
        department.setId("d1");
        department.setOrganizationId(3L);
        department.setName("Sales");
        department.setEmployees(List.of(new Employee("Smith", 25, "engineer")));

        assertThat(department.getId()).isEqualTo("d1");
        assertThat(department.getOrganizationId()).isEqualTo(3L);
        assertThat(department.getName()).isEqualTo("Sales");
        assertThat(department.getEmployees()).hasSize(1);
        assertThat(department.toString()).contains("d1", "Sales");
    }

    @Test
    void employeeKeepsItsFields() {
        Employee employee = new Employee("Johns", 45, "manager");
        employee.setId(9L);

        assertThat(employee.getId()).isEqualTo(9L);
        assertThat(employee.getName()).isEqualTo("Johns");
        assertThat(employee.getAge()).isEqualTo(45);
        assertThat(employee.getPosition()).isEqualTo("manager");
        assertThat(employee.toString()).contains("Johns", "manager");
    }
}
