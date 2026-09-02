package com.example.Employee_OutPass_Project.Controller;

import com.example.Employee_OutPass_Project.DTO.ChangePasswordRequest;
import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/change-password")
    public String showChangePasswordPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("loggedInUser", user);
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(ChangePasswordRequest request,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // ✅ Check minimum password length (4 characters)
            if (request.getNewPassword() == null || request.getNewPassword().length() < 4) {
                throw new RuntimeException("New password must be at least 4 characters long!");
            }

            userService.changePassword(
                    user.getId(),
                    request.getCurrentPassword(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );

            redirectAttributes.addFlashAttribute("successMessage", "✅ Password changed successfully!");
            return "redirect:" + getDashboardRedirect(user);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
            return "redirect:" + getDashboardRedirect(user);
        }
    }

    private String getDashboardRedirect(User user) {
        switch (user.getRole()) {
            case ADMIN:
                return "/admin/dashboard";
            case HOD:
                return "/hod/dashboard";
            case HR:
                return "/hr/dashboard";
            case SECURITY:
                return "/security/dashboard";
            case EMPLOYEE:
                return "/employee/dashboard";
            default:
                return "/dashboard";
        }
    }
}