package com.example.Employee_OutPass_Project.Controller;

import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(HttpSession session, HttpServletResponse response) {
        // ✅ Prevent caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            return redirectBasedOnRole(user);
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model,
                        HttpServletResponse response) {
        // ✅ Prevent caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        try {
            User user = userService.authenticate(username, password);

            if (user == null) {
                model.addAttribute("error", "Invalid username or password!");
                return "login";
            }

            if (!user.isActive()) {
                model.addAttribute("error", "Your account is deactivated. Please contact admin!");
                return "login";
            }

            session.setAttribute("loggedInUser", user);
            return redirectBasedOnRole(user);

        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // ✅ Clear session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // ✅ Prevent caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        return "redirect:/login?logout=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, HttpServletResponse response) {
        // ✅ Prevent caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        return redirectBasedOnRole(user);
    }

    private String redirectBasedOnRole(User user) {
        if (user == null) {
            return "redirect:/login";
        }

        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/admin/dashboard";
            case HOD:
                return "redirect:/hod/dashboard";
            case HR:
                return "redirect:/hr/dashboard";
            case SECURITY:
                return "redirect:/security/dashboard";
            case EMPLOYEE:
                return "redirect:/employee/dashboard";
            default:
                return "redirect:/login";
        }
    }
}