package com.example.Employee_OutPass_Project.Repository;

import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Entity.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ===== Basic Queries =====
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPassword(String username, String password);

    // ✅ REMOVED - No email field
    // Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    // ===== By Department =====
    List<User> findByDepartmentId(Long departmentId);
    List<User> findByDepartmentIdAndRole(Long departmentId, Role role);

    // ===== By Role =====
    List<User> findByRole(Role role);
    List<User> findByRoleAndActiveTrue(Role role);

    // ===== By Status =====
    List<User> findByActiveTrue();
    List<User> findByActiveFalse();

    // ===== Count by Role =====
    long countByRole(Role role);

    // ===== Search =====
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);

    // ===== Get HODs =====
    @Query("SELECT u FROM User u WHERE u.role = 'HOD' AND u.department.id = :departmentId")
    Optional<User> findHODByDepartmentId(@Param("departmentId") Long departmentId);

    // ===== Check if user exists =====
    boolean existsByUsername(String username);

    // ✅ REMOVED - No email field
    // boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);
}