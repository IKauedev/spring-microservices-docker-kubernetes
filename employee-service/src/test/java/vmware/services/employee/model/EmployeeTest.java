package vmware.services.employee.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeTest {

    @Test
    void constructorSetsAllFieldsExceptId() {
        Employee employee = new Employee(1L, 2L, "Smith", 25, "engineer");

        assertThat(employee.getId()).isNull();
        assertThat(employee.getOrganizationId()).isEqualTo(1L);
        assertThat(employee.getDepartmentId()).isEqualTo(2L);
        assertThat(employee.getName()).isEqualTo("Smith");
        assertThat(employee.getAge()).isEqualTo(25);
        assertThat(employee.getPosition()).isEqualTo("engineer");
    }

    @Test
    void settersUpdateTheFields() {
        Employee employee = new Employee();
        employee.setId("abc");
        employee.setOrganizationId(10L);
        employee.setDepartmentId(20L);
        employee.setName("Johns");
        employee.setAge(45);
        employee.setPosition("manager");

        assertThat(employee.getId()).isEqualTo("abc");
        assertThat(employee.getOrganizationId()).isEqualTo(10L);
        assertThat(employee.getDepartmentId()).isEqualTo(20L);
        assertThat(employee.getName()).isEqualTo("Johns");
        assertThat(employee.getAge()).isEqualTo(45);
        assertThat(employee.getPosition()).isEqualTo("manager");
    }

    @Test
    void toStringMentionsIdentityFields() {
        Employee employee = new Employee(1L, 2L, "Smith", 25, "engineer");
        employee.setId("e1");

        assertThat(employee.toString()).contains("e1", "Smith", "engineer");
    }
}
