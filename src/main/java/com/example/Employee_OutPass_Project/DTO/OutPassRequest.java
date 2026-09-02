package com.example.Employee_OutPass_Project.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class OutPassRequest {
    private String employeeName;
    private String employeeId;
    private LocalDate passDate;
    private LocalTime outTime;           // Changed from LocalDateTime
    private LocalTime expectedInTime;    // Changed from LocalDateTime
    private String department;
    private String reason;
}