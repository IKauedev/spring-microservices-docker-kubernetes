package vmware.services.organization.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import vmware.services.organization.model.Organization;

public interface OrganizationRepository
        extends ListCrudRepository<Organization, String>, PagingAndSortingRepository<Organization, String> {

    Page<Organization> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
