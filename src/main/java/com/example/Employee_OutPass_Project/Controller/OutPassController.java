package com.example.Employee_OutPass_Project.Controller;

import com.example.Employee_OutPass_Project.Entity.OutPass;
import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Service.OutPassService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/outpass")
@RequiredArgsConstructor
public class OutPassController {

    private final OutPassService outPassService;

    @GetMapping("/view/{id}")
    public String viewOutPass(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        try {
            OutPass outPass = outPassService.getOutPassById(id);

            // Check permission
            if (user.getRole() == User.Role.EMPLOYEE && !outPass.getEmployeeName().equals(user.getFullName())) {
                model.addAttribute("error", "You can only view your own requests.");
                return "error";
            }

            model.addAttribute("outPass", outPass);
            model.addAttribute("loggedInUser", user);
            return "view";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/print/{id}")
    public String printOutPass(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        try {
            OutPass outPass = outPassService.getOutPassById(id);
            if (!outPass.isAuthorized()) {
                model.addAttribute("error", "Only authorized passes can be printed.");
                return "error";
            }
            model.addAttribute("outPass", outPass);
            return "print";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}