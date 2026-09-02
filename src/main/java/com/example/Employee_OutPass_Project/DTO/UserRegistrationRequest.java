package com.example.Employee_OutPass_Project.DTO;

import lombok.Data;

@Data
public class UserRegistrationRequest {

    private String username;
    private String password;
    private String fullName;
    private String employeeId;
    private Long departmentId;
    private String role;

}