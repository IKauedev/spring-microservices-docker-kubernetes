package vmware.services.department.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import vmware.services.department.model.Department;

import java.util.List;

public interface DepartmentRepository
		extends ListCrudRepository<Department, String>, PagingAndSortingRepository<Department, String> {

	List<Department> findByOrganizationId(Long organizationId);

	long countByOrganizationId(Long organizationId);

	Page<Department> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
