package com.example.Employee_OutPass_Project.Controller;

import com.example.Employee_OutPass_Project.Entity.OutPass;
import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Entity.User.Role;
import com.example.Employee_OutPass_Project.Service.OutPassService;
import com.example.Employee_OutPass_Project.Service.DepartmentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hod")
@RequiredArgsConstructor
public class HODController {

    private final OutPassService outPassService;
    private final DepartmentService departmentService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() != Role.HOD) {
            model.addAttribute("error", "Access denied. Only HOD can access this page.");
            return "error";
        }
        if (user.getDepartment() == null) {
            model.addAttribute("error", "You are not assigned to any department.");
            return "error";
        }

        try {
            List<OutPass> allDeptPasses = outPassService.getPassesByDepartment(
                    user.getDepartment().getName()
            );

            List<OutPass> pendingPasses = outPassService.getPendingHODByDepartment(
                    user.getDepartment().getName()
            );

            long pendingCount = pendingPasses != null ? pendingPasses.size() : 0;
            long totalCount = allDeptPasses != null ? allDeptPasses.size() : 0;

            long approvedCount = allDeptPasses != null ?
                    allDeptPasses.stream().filter(p -> p.isAuthorized()).count() : 0;

            long rejectedCount = allDeptPasses != null ?
                    allDeptPasses.stream().filter(p -> p.isRejected()).count() : 0;

            System.out.println("===== HOD DASHBOARD DEBUG =====");
            System.out.println("Department: " + user.getDepartment().getName());
            System.out.println("Total: " + totalCount);
            System.out.println("Pending HOD: " + pendingCount);
            System.out.println("Approved: " + approvedCount);
            System.out.println("Rejected: " + rejectedCount);
            System.out.println("================================");

            model.addAttribute("loggedInUser", user);
            model.addAttribute("department", user.getDepartment());
            model.addAttribute("pendingPasses", pendingPasses);
            model.addAttribute("allDeptPasses", allDeptPasses);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);

            return "dashboard/hod";

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/authorize")
    public String authorize(@RequestParam Long passId,
                            @RequestParam(required = false) String remarks,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.HOD) {
            return "redirect:/login";
        }

        try {
            OutPass authorized = outPassService.authorizeHOD(passId, user, remarks != null ? remarks : "Approved by HOD");
            System.out.println("✅ Authorized OutPass ID: " + passId + " by " + user.getFullName());
            System.out.println("✅ New Status: " + authorized.getOverallStatus());

            // ✅ Use flash attribute - clears on refresh
            redirectAttributes.addFlashAttribute("successMessage", "✅ Out Pass #" + passId + " authorized successfully!");
            return "redirect:/hod/dashboard";
        } catch (Exception e) {
            System.out.println("❌ Error authorizing: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
            return "redirect:/hod/dashboard";
        }
    }

    @PostMapping("/reject")
    public String reject(@RequestParam Long passId,
                         @RequestParam String remarks,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.HOD) {
            return "redirect:/login";
        }

        try {
            if (remarks == null || remarks.trim().isEmpty()) {
                throw new RuntimeException("Please provide a reason for rejection.");
            }

            OutPass rejected = outPassService.rejectHOD(passId, user, remarks);
            System.out.println("❌ Rejected OutPass ID: " + passId + " by " + user.getFullName());
            System.out.println("❌ New Status: " + rejected.getOverallStatus());

            // ✅ Use flash attribute - clears on refresh
            redirectAttributes.addFlashAttribute("successMessage", "❌ Out Pass #" + passId + " rejected successfully!");
            return "redirect:/hod/dashboard";
        } catch (Exception e) {
            System.out.println("❌ Error rejecting: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
            return "redirect:/hod/dashboard";
        }
    }


}