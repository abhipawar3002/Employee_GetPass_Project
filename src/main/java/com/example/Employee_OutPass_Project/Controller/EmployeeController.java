package com.example.Employee_OutPass_Project.Controller;

import com.example.Employee_OutPass_Project.DTO.OutPassRequest;
import com.example.Employee_OutPass_Project.Entity.OutPass;
import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Entity.User.Role;
import com.example.Employee_OutPass_Project.Service.DepartmentService;
import com.example.Employee_OutPass_Project.Service.OutPassService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final OutPassService outPassService;
    private final DepartmentService departmentService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.EMPLOYEE) {
            return "redirect:/login";
        }

        OutPassRequest request = new OutPassRequest();
        request.setEmployeeName(user.getFullName());
        request.setEmployeeId(user.getEmployeeId());

        model.addAttribute("loggedInUser", user);
        model.addAttribute("outPassRequest", request);
        model.addAttribute("departments", departmentService.getActiveDepartments());

        String employeeName = user.getFullName();
        Long userId = user.getId();
        String employeeId = user.getEmployeeId();

        System.out.println("===== EMPLOYEE DASHBOARD =====");
        System.out.println("User: " + user.getUsername());
        System.out.println("User Full Name: '" + employeeName + "'");
        System.out.println("User Employee ID: '" + employeeId + "'");
        System.out.println("User ID: " + userId);

        List<OutPass> myRequests = null;

        // ✅ FIRST: Try getting by employee name
        myRequests = outPassService.getPassesByEmployeeName(employeeName);
        System.out.println("By Employee Name found: " + (myRequests != null ? myRequests.size() : 0));

        // ✅ SECOND: If no requests found by name, try by employee ID
        if ((myRequests == null || myRequests.isEmpty()) && employeeId != null && !employeeId.isEmpty()) {
            System.out.println("No requests found by name, trying by employee ID: " + employeeId);
            myRequests = outPassService.getPassesByEmployeeId(employeeId);
            System.out.println("By Employee ID found: " + (myRequests != null ? myRequests.size() : 0));
        }

        // ✅ THIRD: If still no requests, try by created_by user ID (MOST RELIABLE)
        if (myRequests == null || myRequests.isEmpty()) {
            System.out.println("No requests found by employee ID, trying by created_by: " + userId);
            myRequests = outPassService.getPassesByCreatedBy(userId);
            System.out.println("By Created By found: " + (myRequests != null ? myRequests.size() : 0));
        }

        // ✅ FOURTH: If still no requests, try getting ALL and filtering (fallback)
        if (myRequests == null || myRequests.isEmpty()) {
            System.out.println("No requests found by created_by, trying fallback - get all and filter");
            List<OutPass> allPasses = outPassService.getAllOutPasses();
            if (allPasses != null) {
                myRequests = allPasses.stream()
                        .filter(p -> p.getCreatedBy() != null && p.getCreatedBy().getId().equals(userId))
                        .collect(java.util.stream.Collectors.toList());
                System.out.println("Fallback filter found: " + myRequests.size());
            }
        }

        System.out.println("Total Requests Found: " + (myRequests != null ? myRequests.size() : 0));
        if (myRequests != null && !myRequests.isEmpty()) {
            for (OutPass p : myRequests) {
                System.out.println("  - ID: " + p.getId() + " | Name: '" + p.getEmployeeName() + "' | EmployeeID: '" + p.getEmployeeId() + "' | CreatedBy: " + (p.getCreatedBy() != null ? p.getCreatedBy().getId() : "null") + " | Status: " + p.getOverallStatus());
            }
        }
        System.out.println("=============================");

        model.addAttribute("myRequests", myRequests != null ? myRequests : List.of());
        model.addAttribute("myRequestsCount", myRequests != null ? myRequests.size() : 0);

        long pending = myRequests != null ? myRequests.stream().filter(p -> p.isPending()).count() : 0;
        long authorized = myRequests != null ? myRequests.stream().filter(p -> p.isAuthorized()).count() : 0;
        long rejected = myRequests != null ? myRequests.stream()
                .filter(p -> p.getOverallStatus() == OutPass.OverallStatus.REJECTED)
                .count() : 0;

        model.addAttribute("myPending", pending);
        model.addAttribute("myAuthorized", authorized);
        model.addAttribute("myRejected", rejected);

        return "dashboard/employee";
    }

    @PostMapping("/apply")
    public String applyOutPass(@ModelAttribute OutPassRequest request,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        System.out.println("===== APPLY DEBUG =====");
        System.out.println("User: " + user.getUsername());
        System.out.println("User ID: " + user.getId());
        System.out.println("Employee Name from form: '" + request.getEmployeeName() + "'");
        System.out.println("Employee ID from form: '" + request.getEmployeeId() + "'");
        System.out.println("Department: " + request.getDepartment());
        System.out.println("Date: " + request.getPassDate());
        System.out.println("Out Time: " + request.getOutTime());
        System.out.println("In Time: " + request.getExpectedInTime());
        System.out.println("Reason: " + request.getReason());
        System.out.println("=======================");

        try {
            // ✅ ALWAYS use logged-in user's name (trimmed)
            request.setEmployeeName(user.getFullName().trim());

            // ✅ Set Employee ID from logged-in user if not provided
            if (request.getEmployeeId() == null || request.getEmployeeId().isEmpty()) {
                request.setEmployeeId(user.getEmployeeId());
            }

            // ✅ Validate required fields
            if (request.getDepartment() == null || request.getDepartment().isEmpty()) {
                throw new RuntimeException("Department is required!");
            }

            if (request.getPassDate() == null) {
                throw new RuntimeException("Date is required!");
            }

            if (request.getOutTime() == null) {
                throw new RuntimeException("Out Time is required!");
            }

            if (request.getExpectedInTime() == null) {
                throw new RuntimeException("Expected In Time is required!");
            }

            if (request.getExpectedInTime().isBefore(request.getOutTime())) {
                throw new RuntimeException("Expected In Time must be after Out Time!");
            }

            OutPass saved = outPassService.createOutPass(
                    request.getEmployeeName(),
                    request.getEmployeeId(),
                    request.getPassDate(),
                    request.getOutTime(),
                    request.getExpectedInTime(),
                    request.getDepartment(),
                    request.getReason(),
                    user  // ✅ This links the request to the current user
            );

            System.out.println("✅ SAVED! OutPass ID: " + saved.getId());
            System.out.println("✅ Employee Name in DB: '" + saved.getEmployeeName() + "'");
            System.out.println("✅ Employee ID in DB: '" + saved.getEmployeeId() + "'");
            System.out.println("✅ Created By in DB: " + (saved.getCreatedBy() != null ? saved.getCreatedBy().getId() : "null"));

            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Out Pass #" + saved.getId() + " submitted successfully! Waiting for HOD authorization.");
            return "redirect:/employee/dashboard";

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("outPassRequest", request);
            redirectAttributes.addFlashAttribute("activeTab", "apply");
            return "redirect:/employee/dashboard";
        }
    }

    @PostMapping("/submit")
    public String submitOutPass(@ModelAttribute OutPassRequest request,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        return applyOutPass(request, session, redirectAttributes);
    }
}