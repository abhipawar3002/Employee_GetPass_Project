package com.example.Employee_OutPass_Project.Repository;

import com.example.Employee_OutPass_Project.Entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);
    Optional<Department> findByDepartmentHeadUsername(String username);
    List<Department> findByActiveTrue();

    // ✅ Add this method
    boolean existsByName(String name);

    boolean existsByDepartmentHeadUsername(String username);
    long countByActiveTrue();
}