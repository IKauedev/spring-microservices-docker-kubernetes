package vmware.services.employee.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import vmware.services.employee.dto.EmployeeStats;
import vmware.services.employee.dto.PageResponse;
import vmware.services.employee.model.Employee;
import vmware.services.employee.service.EmployeeService;

import java.util.List;
import java.util.Map;

@RestController
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping("/")
    @Operation(summary = "Cria um employee")
    public Employee add(@Valid @RequestBody Employee employee) {
        return service.create(employee);
    }

    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista todos os employees")
    public List<Employee> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um employee pelo id")
    public Employee findById(@PathVariable("id") String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substitui um employee existente")
    public Employee update(@PathVariable("id") String id, @Valid @RequestBody Employee employee) {
        return service.update(id, employee);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um employee")
    public void delete(@PathVariable("id") String id) {
        service.delete(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Busca paginada por nome (parcial) e/ou cargo (exato), ordenada por nome")
    public PageResponse<Employee> search(@RequestParam(name = "name", required = false) String name,
                                         @RequestParam(name = "position", required = false) String position,
                                         @RequestParam(name = "page", defaultValue = "0") int page,
                                         @RequestParam(name = "size", defaultValue = "20") int size) {
        return PageResponse.of(service.search(name, position, page, size));
    }

    @GetMapping("/count")
    @Operation(summary = "Total de employees")
    public Map<String, Long> count() {
        return Map.of("count", service.count());
    }

    @GetMapping("/stats")
    @Operation(summary = "Total, idade media e contagem por cargo")
    public EmployeeStats stats() {
        return service.stats();
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Employees de um departamento")
    public List<Employee> findByDepartment(@PathVariable("departmentId") Long departmentId) {
        return service.findByDepartment(departmentId);
    }

    @GetMapping("/department/{departmentId}/count")
    @Operation(summary = "Quantidade de employees de um departamento")
    public Map<String, Long> countByDepartment(@PathVariable("departmentId") Long departmentId) {
        return Map.of("count", service.countByDepartment(departmentId));
    }

    @DeleteMapping("/department/{departmentId}")
    @Operation(summary = "Remove todos os employees de um departamento")
    public Map<String, Long> deleteByDepartment(@PathVariable("departmentId") Long departmentId) {
        return Map.of("deleted", service.deleteByDepartment(departmentId));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Employees de uma organizacao")
    public List<Employee> findByOrganization(@PathVariable("organizationId") Long organizationId) {
        return service.findByOrganization(organizationId);
    }

    @GetMapping("/organization/{organizationId}/count")
    @Operation(summary = "Quantidade de employees de uma organizacao")
    public Map<String, Long> countByOrganization(@PathVariable("organizationId") Long organizationId) {
        return Map.of("count", service.countByOrganization(organizationId));
    }
}
