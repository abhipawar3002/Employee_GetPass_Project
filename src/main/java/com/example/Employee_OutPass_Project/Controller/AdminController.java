package com.example.Employee_OutPass_Project.Controller;

import com.example.Employee_OutPass_Project.Entity.Department;
import com.example.Employee_OutPass_Project.Entity.OutPass;
import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Entity.User.Role;
import com.example.Employee_OutPass_Project.Service.DepartmentService;
import com.example.Employee_OutPass_Project.Service.ExcelExportService;
import com.example.Employee_OutPass_Project.Service.OutPassService;
import com.example.Employee_OutPass_Project.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OutPassService outPassService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final ExcelExportService excelExportService;

    // ✅ Constructor
    public AdminController(OutPassService outPassService, UserService userService,
                           DepartmentService departmentService, ExcelExportService excelExportService) {
        this.outPassService = outPassService;
        this.userService = userService;
        this.departmentService = departmentService;
        this.excelExportService = excelExportService;
    }

    // ===== ADMIN DASHBOARD =====
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String tab,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String department,
                            HttpSession session,
                            Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        loadAdminData(model);

        // Apply filters
        List<OutPass> allPasses = outPassService.getAllOutPasses();

        if (fromDate != null) {
            allPasses = allPasses.stream()
                    .filter(p -> p.getPassDate() != null && !p.getPassDate().isBefore(fromDate))
                    .collect(Collectors.toList());
        }
        if (toDate != null) {
            allPasses = allPasses.stream()
                    .filter(p -> p.getPassDate() != null && !p.getPassDate().isAfter(toDate))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            if (status.equals("APPROVED")) {
                allPasses = allPasses.stream().filter(OutPass::isAuthorized).collect(Collectors.toList());
            } else if (status.equals("REJECTED")) {
                allPasses = allPasses.stream().filter(OutPass::isRejected).collect(Collectors.toList());
            } else if (status.equals("PENDING_HOD") || status.equals("PENDING")) {
                allPasses = allPasses.stream().filter(OutPass::isPending).collect(Collectors.toList());
            }
        }
        if (department != null && !department.isEmpty()) {
            allPasses = allPasses.stream()
                    .filter(p -> p.getDepartment() != null && department.equals(p.getDepartment().getName()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("allPasses", allPasses);
        model.addAttribute("totalCount", allPasses.size());
        model.addAttribute("filterFromDate", fromDate);
        model.addAttribute("filterToDate", toDate);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterDepartment", department);
        model.addAttribute("loggedInUser", user);

        if (tab != null && !tab.isEmpty()) {
            model.addAttribute("activeTab", tab);
        } else {
            model.addAttribute("activeTab", "dashboard");
        }

        return "admin";
    }

    // ===== USER MANAGEMENT =====
    @PostMapping("/users/add")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String fullName,
                          @RequestParam(required = false) String employeeId,
                          @RequestParam(required = false) String department,
                          @RequestParam String role,
                          HttpSession session,
                          Model model) {
        User adminUser = (User) session.getAttribute("loggedInUser");
        if (adminUser == null || adminUser.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        try {
            if (username == null || username.trim().isEmpty()) {
                throw new RuntimeException("Username is required!");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new RuntimeException("Password is required!");
            }
            if (fullName == null || fullName.trim().isEmpty()) {
                throw new RuntimeException("Full name is required!");
            }

            if (userService.isUsernameExists(username.trim())) {
                throw new RuntimeException("Username '" + username + "' already exists!");
            }

            User.Role userRole = User.Role.valueOf(role.toUpperCase());

            User savedUser = userService.registerUser(
                    username.trim(),
                    password,
                    fullName.trim(),
                    employeeId != null ? employeeId.trim() : null,
                    department != null && !department.isEmpty() ? department : null,
                    userRole
            );

            model.addAttribute("userSuccess", "✅ User '" + username + "' added successfully!");
            System.out.println("✅ User added successfully: " + savedUser.getUsername());

        } catch (Exception e) {
            model.addAttribute("userError", "❌ " + e.getMessage());
            System.out.println("❌ Error adding user: " + e.getMessage());
        }

        loadAdminData(model);
        model.addAttribute("loggedInUser", adminUser);
        model.addAttribute("activeTab", "users");
        return "dashboard/admin";
    }

    @GetMapping("/users/deactivate/{id}")
    public String deactivateUser(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        try {
            userService.deactivateUser(id);
            model.addAttribute("userSuccess", "✅ User deactivated successfully!");
        } catch (Exception e) {
            model.addAttribute("userError", "❌ " + e.getMessage());
        }

        loadAdminData(model);
        model.addAttribute("loggedInUser", user);
        model.addAttribute("activeTab", "users");
        return "dashboard/admin";
    }

    @GetMapping("/users/activate/{id}")
    public String activateUser(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        try {
            userService.activateUser(id);
            model.addAttribute("userSuccess", "✅ User activated successfully!");
        } catch (Exception e) {
            model.addAttribute("userError", "❌ " + e.getMessage());
        }

        loadAdminData(model);
        model.addAttribute("loggedInUser", user);
        model.addAttribute("activeTab", "users");
        return "dashboard/admin";
    }

    // ===== DEPARTMENT MANAGEMENT =====
    @PostMapping("/departments/add")
    public String addDepartment(@RequestParam String name,
                                @RequestParam(required = false) String description,
                                HttpSession session,
                                Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        try {
            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("Department name is required!");
            }

            Department dept = departmentService.createDepartment(name.trim(), description, null);
            model.addAttribute("deptSuccess", "✅ Department '" + name + "' added successfully!");
        } catch (Exception e) {
            model.addAttribute("deptError", "❌ " + e.getMessage());
        }

        loadAdminData(model);
        model.addAttribute("loggedInUser", user);
        model.addAttribute("activeTab", "departments");
        return "dashboard/admin";
    }

    @PostMapping("/departments/update-head")
    public String updateDepartmentHead(@RequestParam Long departmentId,
                                       @RequestParam(required = false) String headUsername,
                                       HttpSession session,
                                       Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/login";
        }

        try {
            Department dept = departmentService.updateDepartmentHead(departmentId, headUsername);
            if (headUsername != null && !headUsername.trim().isEmpty()) {
                model.addAttribute("deptSuccess", "✅ HOD assigned successfully to '" + dept.getName() + "' department!");
            } else {
                model.addAttribute("deptSuccess", "✅ HOD removed from '" + dept.getName() + "' department.");
            }
        } catch (Exception e) {
            model.addAttribute("deptError", "❌ " + e.getMessage());
        }

        loadAdminData(model);
        model.addAttribute("loggedInUser", user);
        model.addAttribute("activeTab", "departments");
        return "dashboard/admin";
    }

    // ===== EXCEL EXPORT - SINGLE METHOD WITH FILTERS =====
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<OutPass> allPasses = outPassService.getAllOutPasses();

            // Apply filters
            if (fromDate != null) {
                allPasses = allPasses.stream()
                        .filter(p -> p.getPassDate() != null && !p.getPassDate().isBefore(fromDate))
                        .collect(Collectors.toList());
            }
            if (toDate != null) {
                allPasses = allPasses.stream()
                        .filter(p -> p.getPassDate() != null && !p.getPassDate().isAfter(toDate))
                        .collect(Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                if (status.equals("APPROVED")) {
                    allPasses = allPasses.stream().filter(OutPass::isAuthorized).collect(Collectors.toList());
                } else if (status.equals("REJECTED")) {
                    allPasses = allPasses.stream().filter(OutPass::isRejected).collect(Collectors.toList());
                } else if (status.equals("PENDING_HOD") || status.equals("PENDING")) {
                    allPasses = allPasses.stream().filter(OutPass::isPending).collect(Collectors.toList());
                }
            }
            if (department != null && !department.isEmpty()) {
                allPasses = allPasses.stream()
                        .filter(p -> p.getDepartment() != null && department.equals(p.getDepartment().getName()))
                        .collect(Collectors.toList());
            }

            if (allPasses == null || allPasses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            StringBuilder title = new StringBuilder("OutPass Report");
            if (fromDate != null) title.append(" - From: ").append(fromDate);
            if (toDate != null) title.append(" - To: ").append(toDate);
            if (status != null && !status.isEmpty()) title.append(" - Status: ").append(status);
            if (department != null && !department.isEmpty()) title.append(" - Dept: ").append(department);

            ByteArrayInputStream excelStream = excelExportService.exportOutPassesToExcel(
                    allPasses,
                    title.toString(),
                    user,
                    fromDate,
                    toDate,
                    status
            );

            if (excelStream == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            byte[] excelBytes = excelStream.readAllBytes();
            HttpHeaders headers = new HttpHeaders();

            String filename = "OutPass_Report_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            headers.add("Content-Disposition", "attachment; filename=" + filename);
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok().headers(headers).body(excelBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== HELPER METHOD =====
    private void loadAdminData(Model model) {
        List<Department> departments = departmentService.getAllDepartments();
        List<User> users = userService.getAllUsers();
        List<OutPass> allPasses = outPassService.getAllOutPasses();

        model.addAttribute("departments", departments);
        model.addAttribute("users", users);
        model.addAttribute("allPasses", allPasses);
        model.addAttribute("totalCount", outPassService.getTotalCount());
        model.addAttribute("pendingCount", outPassService.getPendingCount());
        model.addAttribute("approvedCount", outPassService.getApprovedCount());
        model.addAttribute("rejectedCount", outPassService.getRejectedCount());
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("departmentCount", departmentService.countDepartments());
    }
}