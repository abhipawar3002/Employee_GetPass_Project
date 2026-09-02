package com.example.Employee_OutPass_Project.Service;

import com.example.Employee_OutPass_Project.Entity.Department;
import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Repository.DepartmentRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    // ✅ Constructor injection with @Lazy
    public DepartmentService(DepartmentRepository departmentRepository, @Lazy UserService userService) {
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    // ===== GET METHODS =====
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<Department> getActiveDepartments() {
        return departmentRepository.findByActiveTrue();
    }

    public Department getDepartmentByName(String name) {
        return departmentRepository.findByName(name).orElse(null);
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id).orElse(null);
    }

    public Department getDepartmentByIdOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found!"));
    }

    // ===== CREATE =====
    @Transactional
    public Department createDepartment(String name, String description, String departmentHead) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Department name is required!");
        }

        if (departmentRepository.existsByName(name.trim())) {
            throw new RuntimeException("Department with name '" + name + "' already exists!");
        }

        Department department = new Department();
        department.setName(name.trim());
        department.setDescription(description != null ? description.trim() : null);
        department.setDepartmentHead(departmentHead);
        department.setActive(true);
        department.setCreatedAt(LocalDateTime.now());

        System.out.println("✅ Creating department: " + name);

        if (departmentHead != null && !departmentHead.trim().isEmpty()) {
            User hod = userService.getUserByUsername(departmentHead.trim());
            if (hod != null) {
                department.setDepartmentHeadUsername(hod.getUsername());
                if (hod.getRole() != User.Role.HOD) {
                    userService.updateUserRole(hod.getId(), User.Role.HOD);
                }
            }
        }

        return departmentRepository.save(department);
    }

    // ===== UPDATE =====
    @Transactional
    public Department updateDepartment(Long id, String name, String description,
                                       String departmentHead, Boolean active) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found!"));

        if (name != null && !name.isEmpty()) {
            if (departmentRepository.existsByName(name) && !department.getName().equals(name)) {
                throw new RuntimeException("Department with name '" + name + "' already exists!");
            }
            department.setName(name);
        }
        if (description != null) {
            department.setDescription(description);
        }
        if (departmentHead != null) {
            department.setDepartmentHead(departmentHead);
        }
        if (active != null) {
            department.setActive(active);
        }
        department.setUpdatedAt(LocalDateTime.now());

        return departmentRepository.save(department);
    }

    // ===== UPDATE DEPARTMENT HEAD =====
    @Transactional
    public Department updateDepartmentHead(Long departmentId, String headUsername) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found!"));

        if (department.getDepartmentHeadUsername() != null) {
            User oldHod = userService.getUserByUsername(department.getDepartmentHeadUsername());
            if (oldHod != null && oldHod.getRole() == User.Role.HOD) {
                userService.updateUserRole(oldHod.getId(), User.Role.EMPLOYEE);
            }
        }

        if (headUsername != null && !headUsername.trim().isEmpty()) {
            User newHod = userService.getUserByUsername(headUsername.trim());
            if (newHod == null) {
                throw new RuntimeException("User '" + headUsername + "' not found!");
            }
            department.setDepartmentHeadUsername(headUsername.trim());
            department.setDepartmentHead(newHod.getFullName());
            userService.updateUserRole(newHod.getId(), User.Role.HOD);
        } else {
            department.setDepartmentHeadUsername(null);
            department.setDepartmentHead(null);
        }

        department.setUpdatedAt(LocalDateTime.now());
        return departmentRepository.save(department);
    }

    // ===== DELETE =====
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found!"));
        departmentRepository.delete(department);
    }

    // ===== COUNT =====
    public long countDepartments() {
        return departmentRepository.count();
    }

    public long countActiveDepartments() {
        return departmentRepository.countByActiveTrue();
    }
}