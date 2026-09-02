package com.example.Employee_OutPass_Project;

import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Entity.User.Role;
import com.example.Employee_OutPass_Project.Repository.DepartmentRepository;
import com.example.Employee_OutPass_Project.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("🚀 Checking database for initial data...");
        System.out.println("========================================");

        // ONLY CREATE ADMIN USER - No departments pre-loaded
        if (!userRepository.existsByUsername("admin")) {
            createAdminUser();
        } else {
            System.out.println("✅ Admin user already exists.");
        }

        System.out.println("========================================");
        System.out.println("✅ Setup complete!");
        System.out.println("🔑 Login with: admin / admin123");
        System.out.println("📌 Admin can add departments from the Admin Panel");
        System.out.println("========================================");
    }

    private void createAdminUser() {
        System.out.println("👤 Creating Admin user...");

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setFullName("System Administrator");
        admin.setEmployeeId("ADM001");
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        System.out.println("✅ Admin user created!");
        System.out.println("   👤 Username: admin");
        System.out.println("   🔑 Password: admin123");
        System.out.println("   📧 Email: admin@company.com");
    }
}