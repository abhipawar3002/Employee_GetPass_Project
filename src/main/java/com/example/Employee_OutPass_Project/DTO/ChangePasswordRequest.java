package com.example.Employee_OutPass_Project.DTO;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}