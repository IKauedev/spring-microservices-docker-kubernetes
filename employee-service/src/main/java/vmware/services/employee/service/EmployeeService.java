package vmware.services.employee.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vmware.services.employee.dto.EmployeeStats;
import vmware.services.employee.exception.ResourceNotFoundException;
import vmware.services.employee.model.Employee;
import vmware.services.employee.repository.EmployeeRepository;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public Employee create(Employee employee) {
        LOGGER.info("Employee add: {}", employee);
        return repository.save(employee);
    }

    public Employee findById(String id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    public List<Employee> findAll() {
        return repository.findAll();
    }

    public List<Employee> findByDepartment(Long departmentId) {
        return repository.findByDepartmentId(departmentId);
    }

    public List<Employee> findByOrganization(Long organizationId) {
        return repository.findByOrganizationId(organizationId);
    }

    /** Substitui o employee inteiro; o id da URL prevalece sobre o do corpo. */
    public Employee update(String id, Employee employee) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }
        employee.setId(id);
        LOGGER.info("Employee update: {}", employee);
        return repository.save(employee);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }
        LOGGER.info("Employee delete: id={}", id);
        repository.deleteById(id);
    }

    /** Remove todos os employees de um departamento e devolve quantos foram removidos. */
    public long deleteByDepartment(Long departmentId) {
        List<Employee> employees = repository.findByDepartmentId(departmentId);
        repository.deleteAll(employees);
        LOGGER.info("Employee delete: departmentId={}, removed={}", departmentId, employees.size());
        return employees.size();
    }

    public Page<Employee> search(String name, String position, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by("name"));
        boolean hasName = name != null && !name.isBlank();
        boolean hasPosition = position != null && !position.isBlank();
        if (hasName && hasPosition) {
            return repository.findByNameContainingIgnoreCaseAndPositionIgnoreCase(name, position, pageable);
        }
        if (hasName) {
            return repository.findByNameContainingIgnoreCase(name, pageable);
        }
        if (hasPosition) {
            return repository.findByPositionIgnoreCase(position, pageable);
        }
        return repository.findAll(pageable);
    }

    public long count() {
        return repository.count();
    }

    public long countByDepartment(Long departmentId) {
        return repository.countByDepartmentId(departmentId);
    }

    public long countByOrganization(Long organizationId) {
        return repository.countByOrganizationId(organizationId);
    }

    public EmployeeStats stats() {
        List<Employee> all = repository.findAll();
        double averageAge = all.stream().mapToInt(Employee::getAge).average().orElse(0);
        Map<String, Long> byPosition = all.stream()
                .collect(Collectors.groupingBy(e -> e.getPosition() == null ? "unknown" : e.getPosition(),
                        TreeMap::new, Collectors.counting()));
        return new EmployeeStats(all.size(), averageAge, byPosition);
    }
}
