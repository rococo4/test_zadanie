package io.codefresh.gradleexample.store.repository;

import io.codefresh.gradleexample.store.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    public Optional<Employee> findByUsername(String username);
}
