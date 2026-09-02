package com.example.Employee_OutPass_Project.Service;

import com.example.Employee_OutPass_Project.Entity.Department;
import com.example.Employee_OutPass_Project.Entity.OutPass;
import com.example.Employee_OutPass_Project.Entity.OutPass.ApprovalStatus;
import com.example.Employee_OutPass_Project.Entity.OutPass.OverallStatus;
import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Repository.OutPassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutPassService {

    private final OutPassRepository outPassRepository;
    private final DepartmentService departmentService;

    @Transactional
    public OutPass createOutPass(String employeeName, String employeeId, LocalDate passDate,
                                 LocalTime outTime, LocalTime expectedInTime,
                                 String departmentName, String reason, User createdBy) {

        System.out.println("===== CREATING OUT PASS =====");
        System.out.println("Employee Name: " + employeeName);
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Department: " + departmentName);
        System.out.println("Date: " + passDate);
        System.out.println("Out Time: " + outTime);
        System.out.println("In Time: " + expectedInTime);
        System.out.println("Created By User ID: " + (createdBy != null ? createdBy.getId() : "null"));

        Department department = departmentService.getDepartmentByName(departmentName);
        if (department == null) {
            throw new RuntimeException("Department '" + departmentName + "' not found!");
        }

        OutPass outPass = new OutPass();
        outPass.setEmployeeName(employeeName.trim());
        outPass.setEmployeeId(employeeId != null && !employeeId.isEmpty() ? employeeId.trim() : createdBy.getEmployeeId());
        outPass.setPassDate(passDate);
        outPass.setOutTime(outTime);
        outPass.setExpectedInTime(expectedInTime);
        outPass.setDepartment(department);
        outPass.setReason(reason);
        outPass.setCreatedBy(createdBy);
        outPass.setHodStatus(ApprovalStatus.PENDING);
        outPass.setHrStatus(ApprovalStatus.WAITING);
        outPass.setSecurityStatus(ApprovalStatus.WAITING);
        outPass.setOverallStatus(OverallStatus.PENDING_HOD);

        OutPass saved = outPassRepository.save(outPass);
        System.out.println("✅ Saved OutPass ID: " + saved.getId());
        System.out.println("✅ Initial Status: " + saved.getOverallStatus());
        System.out.println("===== END CREATING =====");
        return saved;
    }

    // ===== AUTHORIZE HOD =====
    @Transactional
    public OutPass authorizeHOD(Long passId, User hodUser, String remarks) {
        OutPass outPass = outPassRepository.findById(passId)
                .orElseThrow(() -> new RuntimeException("OutPass not found!"));

        System.out.println("===== AUTHORIZE HOD =====");
        System.out.println("Pass ID: " + passId);
        System.out.println("HOD: " + hodUser.getFullName());
        System.out.println("Current HOD Status: " + outPass.getHodStatus());
        System.out.println("Current Overall Status: " + outPass.getOverallStatus());

        if (hodUser.getDepartment() == null || !hodUser.getDepartment().getId().equals(outPass.getDepartment().getId())) {
            throw new RuntimeException("You can only authorize requests from your department!");
        }

        outPass.setHodStatus(ApprovalStatus.APPROVED);
        outPass.setHodUser(hodUser);
        outPass.setHodName(hodUser.getFullName());
        outPass.setHodRemarks(remarks != null ? remarks : "Approved by HOD");
        outPass.setHodAuthorizedAt(LocalDateTime.now());

        outPass.updateOverallStatus();

        System.out.println("After Update - HOD Status: " + outPass.getHodStatus());
        System.out.println("After Update - Overall Status: " + outPass.getOverallStatus());

        OutPass saved = outPassRepository.save(outPass);
        System.out.println("✅ Saved - Overall Status: " + saved.getOverallStatus());
        System.out.println("===== END AUTHORIZE =====");

        return saved;
    }

    // ===== REJECT HOD =====
    @Transactional
    public OutPass rejectHOD(Long passId, User hodUser, String remarks) {
        OutPass outPass = outPassRepository.findById(passId)
                .orElseThrow(() -> new RuntimeException("OutPass not found!"));

        System.out.println("===== REJECT HOD =====");
        System.out.println("Pass ID: " + passId);
        System.out.println("HOD: " + hodUser.getFullName());
        System.out.println("Current HOD Status: " + outPass.getHodStatus());
        System.out.println("Current Overall Status: " + outPass.getOverallStatus());

        if (hodUser.getDepartment() == null || !hodUser.getDepartment().getId().equals(outPass.getDepartment().getId())) {
            throw new RuntimeException("You can only reject requests from your department!");
        }

        outPass.setHodStatus(ApprovalStatus.REJECTED);
        outPass.setHodUser(hodUser);
        outPass.setHodName(hodUser.getFullName());
        outPass.setHodRemarks(remarks != null ? remarks : "Rejected by HOD");
        outPass.setHodAuthorizedAt(LocalDateTime.now());

        outPass.updateOverallStatus();

        System.out.println("After Update - HOD Status: " + outPass.getHodStatus());
        System.out.println("After Update - Overall Status: " + outPass.getOverallStatus());

        OutPass saved = outPassRepository.save(outPass);
        System.out.println("✅ Saved - Overall Status: " + saved.getOverallStatus());
        System.out.println("===== END REJECT =====");

        return saved;
    }

    // ===== AUTHORIZE HR =====
    @Transactional
    public OutPass authorizeHR(Long passId, User hrUser, String remarks) {
        OutPass outPass = outPassRepository.findById(passId)
                .orElseThrow(() -> new RuntimeException("OutPass not found!"));

        System.out.println("===== AUTHORIZE HR =====");
        System.out.println("Pass ID: " + passId);
        System.out.println("HR: " + hrUser.getFullName());
        System.out.println("Current HR Status: " + outPass.getHrStatus());
        System.out.println("Current Overall Status: " + outPass.getOverallStatus());

        outPass.setHrStatus(ApprovalStatus.APPROVED);
        outPass.setHrUser(hrUser);
        outPass.setHrName(hrUser.getFullName());
        outPass.setHrRemarks(remarks != null ? remarks : "Approved by HR");
        outPass.setHrAuthorizedAt(LocalDateTime.now());

        outPass.updateOverallStatus();

        System.out.println("After Update - HR Status: " + outPass.getHrStatus());
        System.out.println("After Update - Overall Status: " + outPass.getOverallStatus());

        OutPass saved = outPassRepository.save(outPass);
        System.out.println("✅ Saved - Overall Status: " + saved.getOverallStatus());
        System.out.println("===== END AUTHORIZE =====");

        return saved;
    }

    // ===== REJECT HR =====
    @Transactional
    public OutPass rejectHR(Long passId, User hrUser, String remarks) {
        OutPass outPass = outPassRepository.findById(passId)
                .orElseThrow(() -> new RuntimeException("OutPass not found!"));

        System.out.println("===== REJECT HR =====");
        System.out.println("Pass ID: " + passId);
        System.out.println("HR: " + hrUser.getFullName());
        System.out.println("Current HR Status: " + outPass.getHrStatus());
        System.out.println("Current Overall Status: " + outPass.getOverallStatus());

        outPass.setHrStatus(ApprovalStatus.REJECTED);
        outPass.setHrUser(hrUser);
        outPass.setHrName(hrUser.getFullName());
        outPass.setHrRemarks(remarks != null ? remarks : "Rejected by HR");
        outPass.setHrAuthorizedAt(LocalDateTime.now());

        outPass.updateOverallStatus();

        System.out.println("After Update - HR Status: " + outPass.getHrStatus());
        System.out.println("After Update - Overall Status: " + outPass.getOverallStatus());

        OutPass saved = outPassRepository.save(outPass);
        System.out.println("✅ Saved - Overall Status: " + saved.getOverallStatus());
        System.out.println("===== END REJECT =====");

        return saved;
    }

    // ===== SECURITY TIME TRACKING METHODS =====

    @Transactional
    public OutPass markOut(Long passId, User securityUser) {
        OutPass outPass = outPassRepository.findById(passId)
                .orElseThrow(() -> new RuntimeException("OutPass not found!"));

        System.out.println("===== MARK OUT =====");
        System.out.println("Pass ID: " + passId);
        System.out.println("Current Status: " + outPass.getOverallStatus());
        System.out.println("Current Time Status: " + outPass.getTimeStatus());

        // Validate request is authorized
        if (!outPass.isAuthorized()) {
            throw new RuntimeException("This request is not authorized yet!");
        }

        // Check if already out
        if (outPass.isCurrentlyOut()) {
            throw new RuntimeException("Employee is already marked as OUT!");
        }

        // Check if already returned
        if (outPass.hasReturned()) {
            throw new RuntimeException("Employee has already returned!");
        }

        // ✅ Mark as OUT
        outPass.setActualOutTime(LocalDateTime.now());
        outPass.setTimeStatus(OutPass.TimeStatus.OUT);
        outPass.setSecurityUser(securityUser);
        outPass.setSecurityName(securityUser.getFullName());
        outPass.setSecurityAuthorizedAt(LocalDateTime.now());
        outPass.setSecurityStatus(OutPass.ApprovalStatus.APPROVED);

        OutPass saved = outPassRepository.save(outPass);
        System.out.println("✅ Marked OUT: " + saved.getEmployeeName() + " at " + saved.getActualOutTime());
        System.out.println("✅ Time Status: " + saved.getTimeStatus());
        System.out.println("===== END MARK OUT =====");
        return saved;
    }

    @Transactional
    public OutPass markReturn(Long passId, User securityUser) {
        OutPass outPass = outPassRepository.findById(passId)
                .orElseThrow(() -> new RuntimeException("OutPass not found!"));

        System.out.println("===== MARK RETURN =====");
        System.out.println("Pass ID: " + passId);
        System.out.println("Current Status: " + outPass.getOverallStatus());
        System.out.println("Current Time Status: " + outPass.getTimeStatus());

        // Validate request is authorized
        if (!outPass.isAuthorized()) {
            throw new RuntimeException("This request is not authorized yet!");
        }

        // Check if already returned
        if (outPass.hasReturned()) {
            throw new RuntimeException("Employee has already returned!");
        }

        // Check if not out yet
        if (!outPass.isCurrentlyOut()) {
            throw new RuntimeException("Employee is not marked as OUT yet!");
        }

        // ✅ Mark as RETURNED
        outPass.setActualInTime(LocalDateTime.now());

        // Check if overdue (actual in time > expected in time)
        if (outPass.getActualInTime().toLocalTime().isAfter(outPass.getExpectedInTime())) {
            outPass.setTimeStatus(OutPass.TimeStatus.OVERDUE);
            System.out.println("⚠️ Employee is OVERDUE!");
        } else {
            outPass.setTimeStatus(OutPass.TimeStatus.RETURNED);
        }

        outPass.setSecurityUser(securityUser);
        outPass.setSecurityName(securityUser.getFullName());

        OutPass saved = outPassRepository.save(outPass);
        System.out.println("✅ Marked RETURNED: " + saved.getEmployeeName() + " at " + saved.getActualInTime());
        System.out.println("✅ Time Status: " + saved.getTimeStatus());
        System.out.println("===== END MARK RETURN =====");
        return saved;
    }

    // ✅ Get currently out employees
    public List<OutPass> getCurrentlyOutEmployees() {
        return outPassRepository.findByTimeStatus(OutPass.TimeStatus.OUT);
    }

    // ✅ Get today's authorized passes
    public List<OutPass> getTodayAuthorizedPasses() {
        return outPassRepository.findByPassDateAndOverallStatus(LocalDate.now(), OutPass.OverallStatus.APPROVED);
    }

    // ===== GET PASSES BY EMPLOYEE NAME =====
    public List<OutPass> getPassesByEmployeeName(String employeeName) {
        if (employeeName == null || employeeName.isEmpty()) {
            return new ArrayList<>();
        }

        String trimmedName = employeeName.trim();
        List<OutPass> result = outPassRepository.findByEmployeeNameOrderByCreatedAtDesc(trimmedName);

        if (result.isEmpty()) {
            result = outPassRepository.findByEmployeeNameContainingIgnoreCaseOrderByCreatedAtDesc(trimmedName);
        }

        System.out.println("===== GET PASSES BY EMPLOYEE =====");
        System.out.println("Employee Name: '" + trimmedName + "'");
        System.out.println("Results found: " + result.size());
        for (OutPass p : result) {
            System.out.println("  ID: " + p.getId() + " | Status: " + p.getOverallStatus() + " | HOD: " + p.getHodStatus() + " | HR: " + p.getHrStatus());
        }
        System.out.println("=================================");

        return result;
    }

    // ===== GET PASSES BY CREATED BY USER =====
    public List<OutPass> getPassesByCreatedBy(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<OutPass> result = outPassRepository.findByCreatedByIdOrderByCreatedAtDesc(userId);
        System.out.println("===== GET PASSES BY CREATED BY =====");
        System.out.println("User ID: " + userId);
        System.out.println("Results found: " + result.size());
        for (OutPass p : result) {
            System.out.println("  ID: " + p.getId() + " | Created By: " + (p.getCreatedBy() != null ? p.getCreatedBy().getId() : "null") + " | Status: " + p.getOverallStatus());
        }
        System.out.println("=====================================");
        return result;
    }

    // ===== GET ALL PASSES =====
    public List<OutPass> getAllOutPasses() {
        return outPassRepository.findAllByOrderByCreatedAtDesc();
    }

    // ===== HOD METHODS =====
    public List<OutPass> getPendingHODApproval() {
        return outPassRepository.findByOverallStatus(OverallStatus.PENDING_HOD);
    }

    public List<OutPass> getPendingHODByDepartment(String departmentName) {
        Department dept = departmentService.getDepartmentByName(departmentName);
        return outPassRepository.findByDepartmentIdAndOverallStatus(dept.getId(), OverallStatus.PENDING_HOD);
    }

    public List<OutPass> getPassesByDepartment(String departmentName) {
        Department dept = departmentService.getDepartmentByName(departmentName);
        return outPassRepository.findByDepartmentIdOrderByCreatedAtDesc(dept.getId());
    }

    // ===== HR METHODS =====
    public List<OutPass> getPendingHRApproval() {
        return outPassRepository.findByOverallStatus(OverallStatus.PENDING_HR);
    }

    // ===== SECURITY METHODS (VIEW ONLY) =====
    public List<OutPass> getAllOutPassesForSecurity() {
        return outPassRepository.findAllByOrderByCreatedAtDesc();
    }

    public OutPass viewOutPass(Long id) {
        return outPassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OutPass not found!"));
    }

    public OutPass getOutPassById(Long id) {
        return outPassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OutPass not found!"));
    }

    public List<OutPass> getPassesByEmployeeId(String employeeId) {
        return outPassRepository.findByEmployeeId(employeeId);
    }

    public List<OutPass> getPassesByDate(LocalDate date) {
        return outPassRepository.findByPassDate(date);
    }

    // ===== COUNT METHODS =====
    public long getTotalCount() {
        return outPassRepository.count();
    }

    public long getPendingCount() {
        return outPassRepository.findByOverallStatus(OverallStatus.PENDING_HOD).size() +
                outPassRepository.findByOverallStatus(OverallStatus.PENDING_HR).size() +
                outPassRepository.findByOverallStatus(OverallStatus.PENDING_SECURITY).size();
    }

    public long getApprovedCount() {
        return outPassRepository.findByOverallStatus(OverallStatus.APPROVED).size();
    }

    public long getRejectedCount() {
        return outPassRepository.findByOverallStatus(OverallStatus.REJECTED).size();
    }

    public long getPendingByDepartment(Long departmentId) {
        return outPassRepository.findByDepartmentIdAndOverallStatus(departmentId, OverallStatus.PENDING_HOD).size();
    }
}