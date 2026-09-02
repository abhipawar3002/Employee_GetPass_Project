-- =============================================
-- Employee Out Pass System - Database Setup Script
-- =============================================

-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS outpass_db;
USE outpass_db;

-- Step 2: Drop existing tables (if they exist)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS out_passes;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS departments;
SET FOREIGN_KEY_CHECKS = 1;

-- Step 3: Create Departments Table
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    department_head VARCHAR(100),
    department_head_username VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME,
    updated_at DATETIME
);

-- Step 4: Create Users Table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    employee_id VARCHAR(50) UNIQUE,
    department_id BIGINT,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    last_login DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- Step 5: Create Out Passes Table
CREATE TABLE out_passes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_name VARCHAR(100) NOT NULL,
    employee_id VARCHAR(50),
    pass_date DATE NOT NULL,
    out_time TIME NOT NULL,
    expected_in_time TIME NOT NULL,
    department_id BIGINT,
    reason VARCHAR(500) NOT NULL,

    -- HOD Authorization
    hod_status VARCHAR(20) DEFAULT 'PENDING',
    hod_user_id BIGINT,
    hod_name VARCHAR(100),
    hod_remarks VARCHAR(255),
    hod_authorized_at DATETIME,

    -- HR Authorization
    hr_status VARCHAR(20) DEFAULT 'WAITING',
    hr_user_id BIGINT,
    hr_name VARCHAR(100),
    hr_remarks VARCHAR(255),
    hr_authorized_at DATETIME,

    -- Security Authorization
    security_status VARCHAR(20) DEFAULT 'WAITING',
    security_user_id BIGINT,
    security_name VARCHAR(100),
    security_remarks VARCHAR(255),
    security_authorized_at DATETIME,

    overall_status VARCHAR(20) DEFAULT 'PENDING_HOD',
    created_at DATETIME,
    updated_at DATETIME,
    created_by BIGINT,

    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    FOREIGN KEY (hod_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (hr_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (security_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Step 6: Insert Departments
INSERT INTO departments (id, name, description, department_head, department_head_username, active, created_at) VALUES
(1, 'IT', 'Information Technology Department', 'Dr. Rajesh Kumar', 'hod', true, NOW()),
(2, 'HR', 'Human Resources Department', 'Ms. Priya Sharma', 'hr', true, NOW()),
(3, 'Finance', 'Finance Department', 'Mr. Amit Patel', 'finance_head', true, NOW()),
(4, 'Production', 'Production Department', 'Mr. Suresh Singh', 'prod_head', true, NOW()),
(5, 'Sales', 'Sales Department', 'Ms. Neha Gupta', 'sales_head', true, NOW()),
(6, 'Administration', 'Administration Department', 'Mr. Vikram Reddy', 'admin_head', true, NOW());

-- Step 7: Insert Users
-- Password: admin123, hod123, hr123, emp123, sec123
INSERT INTO users (id, username, password, full_name, email, employee_id, department_id, role, active, created_at) VALUES
(1, 'admin', 'admin123', 'System Administrator', 'admin@company.com', 'ADM001', 6, 'ADMIN', true, NOW()),
(2, 'hod', 'hod123', 'Dr. Rajesh Kumar', 'hod@company.com', 'HOD001', 1, 'HOD', true, NOW()),
(3, 'hr', 'hr123', 'Ms. Priya Sharma', 'hr@company.com', 'HR001', 2, 'HR', true, NOW()),
(4, 'employee', 'emp123', 'John Doe', 'employee@company.com', 'EMP001', 1, 'EMPLOYEE', true, NOW()),
(5, 'security', 'sec123', 'Mr. Security Guard', 'security@company.com', 'SEC001', NULL, 'SECURITY', true, NOW()),
(6, 'finance_head', 'hod123', 'Mr. Amit Patel', 'finance@company.com', 'HOD002', 3, 'HOD', true, NOW()),
(7, 'prod_head', 'hod123', 'Mr. Suresh Singh', 'production@company.com', 'HOD003', 4, 'HOD', true, NOW()),
(8, 'sales_head', 'hod123', 'Ms. Neha Gupta', 'sales@company.com', 'HOD004', 5, 'HOD', true, NOW()),
(9, 'admin_head', 'hod123', 'Mr. Vikram Reddy', 'admin@company.com', 'HOD005', 6, 'HOD', true, NOW());

-- Step 8: Insert Sample Out Passes (Optional - for testing)
INSERT INTO out_passes (employee_name, employee_id, pass_date, out_time, expected_in_time, department_id, reason,
                        hod_status, hod_name, hod_remarks, hod_authorized_at,
                        overall_status, created_at, created_by) VALUES
('John Doe', 'EMP001', CURDATE(), '10:00:00', '12:00:00', 1, 'Client meeting at office',
 'APPROVED', 'Dr. Rajesh Kumar', 'Approved for client meeting', NOW(),
 'APPROVED', NOW(), 4),

('Jane Smith', 'EMP002', CURDATE(), '14:00:00', '16:00:00', 2, 'HR training session',
 'PENDING', NULL, NULL, NULL,
 'PENDING_HOD', NOW(), 1),

('Mike Johnson', 'EMP003', CURDATE(), '09:00:00', '11:00:00', 3, 'Bank work',
 'REJECTED', 'Mr. Amit Patel', 'Need proper justification', NOW(),
 'REJECTED', NOW(), 4);

-- Step 9: Verify Data
SELECT '=== DEPARTMENTS ===' as '';
SELECT * FROM departments;

SELECT '=== USERS ===' as '';
SELECT id, username, full_name, role, department_id, active FROM users;

SELECT '=== OUT PASSES ===' as '';
SELECT id, employee_name, department_id, overall_status FROM out_passes;

SELECT '=== SETUP COMPLETE ===' as '';
SELECT 'You can now login with:' as '';
SELECT 'Admin: admin/admin123' as '';
SELECT 'HOD: hod/hod123' as '';
SELECT 'HR: hr/hr123' as '';
SELECT 'Employee: employee/emp123' as '';
SELECT 'Security: security/sec123' as '';