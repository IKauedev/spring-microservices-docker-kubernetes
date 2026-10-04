package vmware.services.department.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vmware.services.department.client.EmployeeClient;
import vmware.services.department.exception.ResourceNotFoundException;
import vmware.services.department.model.Department;
import vmware.services.department.repository.DepartmentRepository;

import java.util.List;

@Service
public class DepartmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentService.class);
	private static final int MAX_PAGE_SIZE = 100;

	private final DepartmentRepository repository;
	private final EmployeeClient employeeClient;

	public DepartmentService(DepartmentRepository repository, EmployeeClient employeeClient) {
		this.repository = repository;
		this.employeeClient = employeeClient;
	}

	public Department create(Department department) {
		LOGGER.info("Department add: {}", department);
		return repository.save(department);
	}

	public Department findById(String id) {
		return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department", id));
	}

	public Department findByIdWithEmployees(String id) {
		Department department = findById(id);
		department.setEmployees(employeeClient.findByDepartment(department.getId()));
		return department;
	}

	public List<Department> findAll() {
		return repository.findAll();
	}

	public List<Department> findByOrganization(Long organizationId) {
		return repository.findByOrganizationId(organizationId);
	}

	public List<Department> findByOrganizationWithEmployees(Long organizationId) {
		List<Department> departments = repository.findByOrganizationId(organizationId);
		departments.forEach(d -> d.setEmployees(employeeClient.findByDepartment(d.getId())));
		return departments;
	}

	/** Substitui o department inteiro; o id da URL prevalece sobre o do corpo. */
	public Department update(String id, Department department) {
		if (!repository.existsById(id)) {
			throw new ResourceNotFoundException("Department", id);
		}
		department.setId(id);
		LOGGER.info("Department update: {}", department);
		return repository.save(department);
	}

	public void delete(String id) {
		if (!repository.existsById(id)) {
			throw new ResourceNotFoundException("Department", id);
		}
		LOGGER.info("Department delete: id={}", id);
		repository.deleteById(id);
	}

	public Page<Department> search(String name, int page, int size) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
				Sort.by("name"));
		if (name == null || name.isBlank()) {
			return repository.findAll(pageable);
		}
		return repository.findByNameContainingIgnoreCase(name, pageable);
	}

	public long count() {
		return repository.count();
	}

	public long countByOrganization(Long organizationId) {
		return repository.countByOrganizationId(organizationId);
	}
}
