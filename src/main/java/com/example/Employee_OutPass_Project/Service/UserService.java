package com.example.Employee_OutPass_Project.Service;

import com.example.Employee_OutPass_Project.Entity.Department;
import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Entity.User.Role;
import com.example.Employee_OutPass_Project.Repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentService departmentService;

    // ✅ Constructor injection with @Lazy
    public UserService(UserRepository userRepository, @Lazy DepartmentService departmentService) {
        this.userRepository = userRepository;
        this.departmentService = departmentService;
    }

    // ===== AUTHENTICATION =====
    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        return userRepository.findByUsernameAndPassword(username, password).orElse(null);
    }

    // ===== GET METHODS =====
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User getUserByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId).orElse(null);
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> getUsersByDepartment(Long departmentId) {
        return userRepository.findByDepartmentId(departmentId);
    }

    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    // ===== REGISTER USER =====
    @Transactional
    public User registerUser(String username, String password, String fullName,
                             String employeeId, String departmentName, Role role) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists!");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmployeeId(employeeId);
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        if (departmentName != null && !departmentName.trim().isEmpty()) {
            Department department = departmentService.getDepartmentByName(departmentName.trim());
            if (department != null) {
                user.setDepartment(department);
            } else {
                System.out.println("⚠️ Department not found: " + departmentName);
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public User registerUser(String username, String password, String fullName,
                             String employeeId, Long departmentId, Role role) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists!");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmployeeId(employeeId);
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        if (departmentId != null) {
            Department department = departmentService.getDepartmentById(departmentId);
            if (department != null) {
                user.setDepartment(department);
            }
        }

        return userRepository.save(user);
    }

    // ===== UPDATE METHODS =====
    @Transactional
    public User updateUser(Long id, String fullName, String employeeId,
                           Long departmentId, Role role, Boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (fullName != null && !fullName.isEmpty()) {
            user.setFullName(fullName);
        }
        if (employeeId != null) {
            user.setEmployeeId(employeeId);
        }
        if (departmentId != null) {
            Department department = departmentService.getDepartmentById(departmentId);
            user.setDepartment(department);
        }
        if (role != null) {
            user.setRole(role);
        }
        if (active != null) {
            user.setActive(active);
        }
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ===== CHANGE PASSWORD =====
    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new RuntimeException("Current password is required!");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("New password is required!");
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            throw new RuntimeException("Please confirm your new password!");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New password and confirm password do not match!");
        }
        if (currentPassword.equals(newPassword)) {
            throw new RuntimeException("New password cannot be same as current password!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!user.getPassword().equals(currentPassword)) {
            throw new RuntimeException("Current password is incorrect!");
        }

        user.setPassword(newPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new RuntimeException("Current password is required!");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("New password is required!");
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            throw new RuntimeException("Please confirm your new password!");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New password and confirm password do not match!");
        }
        if (currentPassword.equals(newPassword)) {
            throw new RuntimeException("New password cannot be same as current password!");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!user.getPassword().equals(currentPassword)) {
            throw new RuntimeException("Current password is incorrect!");
        }

        user.setPassword(newPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    // ===== DELETE / DEACTIVATE =====
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        userRepository.delete(user);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ===== COUNT METHODS =====
    public long countUsers() {
        return userRepository.count();
    }

    public long countUsersByRole(Role role) {
        return userRepository.countByRole(role);
    }

    // ===== CHECK METHODS =====
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmployeeIdExists(String employeeId) {
        if (employeeId == null || employeeId.isEmpty()) {
            return false;
        }
        return userRepository.existsByEmployeeId(employeeId);
    }
}