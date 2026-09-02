// src/main/java/com/example/Employee_OutPass_Project/EmployeeOutPassProjectApplication.java
package com.example.Employee_OutPass_Project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan(basePackages = "com.example.Employee_OutPass_Project.Entity")
@EnableJpaRepositories(basePackages = "com.example.Employee_OutPass_Project.Repository")
public class EmployeeOutPassProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeOutPassProjectApplication.class, args);
    }
}