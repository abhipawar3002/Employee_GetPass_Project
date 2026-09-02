package com.example.Employee_OutPass_Project.Interceptor;

import com.example.Employee_OutPass_Project.Entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // ✅ Prevent browser caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        HttpSession session = request.getSession(false);

        // ✅ Check if user is logged in
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect("/login");
            return false;
        }

        // ✅ Optional: Check if session is still valid
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}