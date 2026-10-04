package vmware.services.department.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vmware.services.department.client.EmployeeClient;
import vmware.services.department.dto.PageResponse;
import vmware.services.department.model.Department;
import vmware.services.department.model.Employee;
import vmware.services.department.service.DepartmentService;

import java.util.List;
import java.util.Map;

@RestController
public class DepartmentController {

	private final DepartmentService service;
	private final EmployeeClient employeeClient;

	public DepartmentController(DepartmentService service, EmployeeClient employeeClient) {
		this.service = service;
		this.employeeClient = employeeClient;
	}

	@GetMapping("/feign")
	@Operation(summary = "Exemplo: employees do departamento 1 via Feign")
	public List<Employee> listRest() {
		return employeeClient.findByDepartment("1");
	}

	@PostMapping("/")
	@Operation(summary = "Cria um department")
	public Department add(@Valid @RequestBody Department department) {
		return service.create(department);
	}

	@GetMapping("/")
	@Operation(summary = "Lista todos os departments")
	public List<Department> findAll() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca um department pelo id")
	public Department findById(@PathVariable("id") String id) {
		return service.findById(id);
	}

	@GetMapping("/{id}/with-employees")
	@Operation(summary = "Department com seus employees (via employee-service)")
	public Department findByIdWithEmployees(@PathVariable("id") String id) {
		return service.findByIdWithEmployees(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Substitui um department existente")
	public Department update(@PathVariable("id") String id, @Valid @RequestBody Department department) {
		return service.update(id, department);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove um department")
	public void delete(@PathVariable("id") String id) {
		service.delete(id);
	}

	@GetMapping("/search")
	@Operation(summary = "Busca paginada por nome (parcial), ordenada por nome")
	public PageResponse<Department> search(@RequestParam(name = "name", required = false) String name,
										   @RequestParam(name = "page", defaultValue = "0") int page,
										   @RequestParam(name = "size", defaultValue = "20") int size) {
		return PageResponse.of(service.search(name, page, size));
	}

	@GetMapping("/count")
	@Operation(summary = "Total de departments")
	public Map<String, Long> count() {
		return Map.of("count", service.count());
	}

	@GetMapping("/organization/{organizationId}")
	@Operation(summary = "Departments de uma organizacao")
	public List<Department> findByOrganization(@PathVariable("organizationId") Long organizationId) {
		return service.findByOrganization(organizationId);
	}

	@GetMapping("/organization/{organizationId}/count")
	@Operation(summary = "Quantidade de departments de uma organizacao")
	public Map<String, Long> countByOrganization(@PathVariable("organizationId") Long organizationId) {
		return Map.of("count", service.countByOrganization(organizationId));
	}

	@GetMapping("/organization/{organizationId}/with-employees")
	@Operation(summary = "Departments de uma organizacao, cada um com seus employees")
	public List<Department> findByOrganizationWithEmployees(@PathVariable("organizationId") Long organizationId) {
		return service.findByOrganizationWithEmployees(organizationId);
	}

}
