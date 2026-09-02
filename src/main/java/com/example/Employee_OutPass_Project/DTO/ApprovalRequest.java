package com.example.Employee_OutPass_Project.DTO;

import lombok.Data;

@Data
public class ApprovalRequest {
    private Long passId;
    private String action; // APPROVE or REJECT
    private String remarks;
}
