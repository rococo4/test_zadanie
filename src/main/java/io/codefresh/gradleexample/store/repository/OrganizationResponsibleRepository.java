package io.codefresh.gradleexample.store.repository;

import io.codefresh.gradleexample.store.entity.Employee;
import io.codefresh.gradleexample.store.entity.OrganizationResponsible;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationResponsibleRepository extends JpaRepository<OrganizationResponsible, UUID> {
    List<OrganizationResponsible> findAllByEmployee_Id(UUID employeeId);
    Optional<OrganizationResponsible> findByEmployee_Id(UUID employeeId);
    List<OrganizationResponsible> findAllByOrganization_Id(UUID organizationId);
}
