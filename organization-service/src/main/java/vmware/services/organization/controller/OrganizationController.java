package vmware.services.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vmware.services.organization.dto.OrganizationSummary;
import vmware.services.organization.dto.PageResponse;
import vmware.services.organization.model.Organization;
import vmware.services.organization.service.OrganizationService;

import java.util.List;
import java.util.Map;

@RestController
public class OrganizationController {

    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cria uma organization")
    public Organization add(@Valid @RequestBody Organization organization) {
        return service.create(organization);
    }

    @GetMapping
    @Operation(summary = "Lista todas as organizations")
    public List<Organization> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma organization pelo id")
    public Organization findById(@PathVariable("id") String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substitui uma organization existente")
    public Organization update(@PathVariable("id") String id, @Valid @RequestBody Organization organization) {
        return service.update(id, organization);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma organization")
    public void delete(@PathVariable("id") String id) {
        service.delete(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Busca paginada por nome (parcial), ordenada por nome")
    public PageResponse<Organization> search(@RequestParam(name = "name", required = false) String name,
                                             @RequestParam(name = "page", defaultValue = "0") int page,
                                             @RequestParam(name = "size", defaultValue = "20") int size) {
        return PageResponse.of(service.search(name, page, size));
    }

    @GetMapping("/count")
    @Operation(summary = "Total de organizations")
    public Map<String, Long> count() {
        return Map.of("count", service.count());
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Resumo: quantidade de departments e employees (via Feign)")
    public OrganizationSummary summary(@PathVariable("id") String id) {
        return service.summary(id);
    }

    @GetMapping("/{id}/with-departments")
    @Operation(summary = "Organization com seus departments")
    public Organization findByIdWithDepartments(@PathVariable("id") String id) {
        return service.findByIdWithDepartments(id);
    }

    @GetMapping("/{id}/with-departments-and-employees")
    @Operation(summary = "Organization com departments e seus employees")
    public Organization findByIdWithDepartmentsAndEmployees(@PathVariable("id") String id) {
        return service.findByIdWithDepartmentsAndEmployees(id);
    }

    @GetMapping("/{id}/with-employees")
    @Operation(summary = "Organization com todos os seus employees")
    public Organization findByIdWithEmployees(@PathVariable("id") String id) {
        return service.findByIdWithEmployees(id);
    }
}
