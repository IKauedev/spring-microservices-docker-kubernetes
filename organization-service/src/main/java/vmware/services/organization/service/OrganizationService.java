package vmware.services.organization.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vmware.services.organization.client.DepartmentClient;
import vmware.services.organization.client.EmployeeClient;
import vmware.services.organization.dto.OrganizationSummary;
import vmware.services.organization.exception.ResourceNotFoundException;
import vmware.services.organization.model.Organization;
import vmware.services.organization.repository.OrganizationRepository;

import java.util.List;

@Service
public class OrganizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final OrganizationRepository repository;
    private final DepartmentClient departmentClient;
    private final EmployeeClient employeeClient;

    public OrganizationService(OrganizationRepository repository, DepartmentClient departmentClient,
                               EmployeeClient employeeClient) {
        this.repository = repository;
        this.departmentClient = departmentClient;
        this.employeeClient = employeeClient;
    }

    public Organization create(Organization organization) {
        LOGGER.info("Organization add: {}", organization);
        return repository.save(organization);
    }

    public List<Organization> findAll() {
        return repository.findAll();
    }

    public Organization findById(String id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Organization", id));
    }

    public Organization findByIdWithDepartments(String id) {
        Organization o = findById(id);
        o.setDepartments(departmentClient.findByOrganization(o.getId()));
        return o;
    }

    public Organization findByIdWithDepartmentsAndEmployees(String id) {
        Organization o = findById(id);
        o.setDepartments(departmentClient.findByOrganizationWithEmployees(o.getId()));
        return o;
    }

    public Organization findByIdWithEmployees(String id) {
        Organization o = findById(id);
        o.setEmployees(employeeClient.findByOrganization(o.getId()));
        return o;
    }

    public OrganizationSummary summary(String id) {
        Organization o = findById(id);
        int departments = departmentClient.findByOrganization(o.getId()).size();
        int employees = employeeClient.findByOrganization(o.getId()).size();
        return new OrganizationSummary(o.getId(), o.getName(), departments, employees);
    }

    /** Substitui a organization inteira; o id da URL prevalece sobre o do corpo. */
    public Organization update(String id, Organization organization) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Organization", id);
        }
        organization.setId(id);
        LOGGER.info("Organization update: {}", organization);
        return repository.save(organization);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Organization", id);
        }
        LOGGER.info("Organization delete: id={}", id);
        repository.deleteById(id);
    }

    public Page<Organization> search(String name, int page, int size) {
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
}
