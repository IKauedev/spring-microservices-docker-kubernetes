package vmware.services.employee.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import vmware.services.employee.model.Employee;

import java.util.List;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByOrganizationId(Long organizationId);

    long countByDepartmentId(Long departmentId);

    long countByOrganizationId(Long organizationId);

    Page<Employee> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Employee> findByPositionIgnoreCase(String position, Pageable pageable);

    Page<Employee> findByNameContainingIgnoreCaseAndPositionIgnoreCase(String name, String position, Pageable pageable);
}
