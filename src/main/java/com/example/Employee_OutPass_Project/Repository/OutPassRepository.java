package com.example.Employee_OutPass_Project.Repository;

import com.example.Employee_OutPass_Project.Entity.OutPass;
import com.example.Employee_OutPass_Project.Entity.OutPass.ApprovalStatus;
import com.example.Employee_OutPass_Project.Entity.OutPass.OverallStatus;
import com.example.Employee_OutPass_Project.Entity.OutPass.TimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OutPassRepository extends JpaRepository<OutPass, Long> {

    // ===== By Status =====
    List<OutPass> findByOverallStatus(OverallStatus status);
    List<OutPass> findByHodStatus(ApprovalStatus status);
    List<OutPass> findByHrStatus(ApprovalStatus status);
    List<OutPass> findBySecurityStatus(ApprovalStatus status);

    // ===== NEW: By Time Status =====
    List<OutPass> findByTimeStatus(TimeStatus status);

    // ===== By Department =====
    List<OutPass> findByDepartmentId(Long departmentId);
    List<OutPass> findByDepartmentIdAndOverallStatus(Long departmentId, OverallStatus status);

    @Query("SELECT o FROM OutPass o WHERE o.department.name = :departmentName")
    List<OutPass> findByDepartmentName(@Param("departmentName") String departmentName);

    // ===== By Employee - Latest First =====
    List<OutPass> findByEmployeeNameOrderByCreatedAtDesc(String employeeName);

    @Query("SELECT o FROM OutPass o WHERE LOWER(o.employeeName) = LOWER(:employeeName) ORDER BY o.createdAt DESC")
    List<OutPass> findByEmployeeNameIgnoreCaseOrderByCreatedAtDesc(@Param("employeeName") String employeeName);

    List<OutPass> findByEmployeeNameContainingIgnoreCaseOrderByCreatedAtDesc(String employeeName);

    @Query("SELECT o FROM OutPass o WHERE TRIM(LOWER(o.employeeName)) = TRIM(LOWER(:employeeName)) ORDER BY o.createdAt DESC")
    List<OutPass> findByEmployeeNameExactIgnoreCaseOrderByCreatedAtDesc(@Param("employeeName") String employeeName);

    List<OutPass> findByEmployeeId(String employeeId);

    // ===== By Created By User =====
    @Query("SELECT o FROM OutPass o WHERE o.createdBy.id = :userId ORDER BY o.createdAt DESC")
    List<OutPass> findByCreatedByIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // ===== By Date =====
    List<OutPass> findByPassDate(LocalDate date);
    List<OutPass> findByPassDateBetween(LocalDate start, LocalDate end);

    // ===== NEW: By Date and Status =====
    List<OutPass> findByPassDateAndOverallStatus(LocalDate date, OverallStatus status);

    // ===== By Authorization =====
    List<OutPass> findByHodUserUsername(String username);
    List<OutPass> findByHrUserUsername(String username);
    List<OutPass> findBySecurityUserUsername(String username);

    // ===== Ordering - Latest First (CreatedAt DESC) =====
    List<OutPass> findAllByOrderByCreatedAtDesc();
    List<OutPass> findByOverallStatusOrderByCreatedAtDesc(OverallStatus status);
    List<OutPass> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId);
    List<OutPass> findByDepartmentIdAndOverallStatusOrderByCreatedAtDesc(Long departmentId, OverallStatus status);

    // ===== Ordering by ID Ascending =====
    List<OutPass> findAllByOrderByIdAsc();

    // ===== Count by Employee =====
    long countByEmployeeName(String employeeName);

    // ===== Get Latest by Employee =====
    List<OutPass> findTop5ByEmployeeNameOrderByCreatedAtDesc(String employeeName);
}