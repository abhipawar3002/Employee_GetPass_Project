package com.example.Employee_OutPass_Project.Config;

import com.example.Employee_OutPass_Project.Interceptor.SecurityInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SecurityInterceptor())
                .addPathPatterns(
                        "/admin/**",
                        "/employee/**",
                        "/hod/**",
                        "/hr/**",
                        "/security/**",
                        "/profile/**"
                )
                .excludePathPatterns(
                        "/login",
                        "/logout",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/",
                        "/index"
                );
    }
}