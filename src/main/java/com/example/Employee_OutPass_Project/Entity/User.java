package com.example.Employee_OutPass_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(unique = true)
    private String employeeId;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    private Role role;

    // ✅ Change from boolean to Boolean for better null handling
    private Boolean active = true;

    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum Role {
        ADMIN,      // Full system access
        HOD,        // Department Head - can authorize
        HR,         // Human Resources
        EMPLOYEE,   // Regular employee
        SECURITY    // Security personnel
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ✅ Getter method for active (explicitly defined)
    public Boolean getActive() {
        return active;
    }

    // ✅ Setter method for active (explicitly defined)
    public void setActive(Boolean active) {
        this.active = active;
    }

    // ✅ Helper method to check if user is active
    public boolean isActive() {
        return active != null && active;
    }

    public boolean isHOD() {
        return this.role == Role.HOD;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isEmployee() {
        return this.role == Role.EMPLOYEE;
    }

    public boolean isHR() {
        return this.role == Role.HR;
    }

    public boolean isSecurity() {
        return this.role == Role.SECURITY;
    }

    public String getDepartmentName() {
        return department != null ? department.getName() : null;
    }
}