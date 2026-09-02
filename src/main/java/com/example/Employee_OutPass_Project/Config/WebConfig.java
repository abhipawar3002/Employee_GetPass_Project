//package com.example.Employee_OutPass_Project.Config;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new HandlerInterceptor() {
//                    @Override
//                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//                        // ✅ Prevent browser caching
//                        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
//                        response.setHeader("Pragma", "no-cache");
//                        response.setHeader("Expires", "0");
//
//                        HttpSession session = request.getSession(false);
//
//                        // ✅ Check if user is logged in
//                        if (session == null || session.getAttribute("loggedInUser") == null) {
//                            response.sendRedirect("/login");
//                            return false;
//                        }
//                        return true;
//                    }
//                })
//                .addPathPatterns(
//                        "/admin/**",
//                        "/employee/**",
//                        "/hod/**",
//                        "/hr/**",
//                        "/security/**",
//                        "/profile/**"
//                )
//                .excludePathPatterns(
//                        "/login",
//                        "/logout",
//                        "/css/**",
//                        "/js/**",
//                        "/images/**",
//                        "/webjars/**",
//                        "/",
//                        "/index"
//                );
//    }
//}