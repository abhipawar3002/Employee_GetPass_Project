// src/main/java/com/example/Employee_OutPass_Project/DTO/DepartmentRequest.java
package com.example.Employee_OutPass_Project.DTO;

import lombok.Data;

@Data
public class DepartmentRequest {
    private String name;
    private String description;
    private String departmentHeadUsername;
}