package com.example.Employee_OutPass_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "out_passes")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OutPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeName;

    private String employeeId;

    @Column(nullable = false)
    private LocalDate passDate;

    @Column(nullable = false)
    private LocalTime outTime;

    @Column(nullable = false)
    private LocalTime expectedInTime;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, length = 500)
    private String reason;

    // Department Head Authorization
    @Enumerated(EnumType.STRING)
    private ApprovalStatus hodStatus = ApprovalStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "hod_user_id")
    private User hodUser;

    private String hodName;
    private String hodRemarks;
    private LocalDateTime hodAuthorizedAt;

    // HR Approval
    @Enumerated(EnumType.STRING)
    private ApprovalStatus hrStatus = ApprovalStatus.WAITING;

    @ManyToOne
    @JoinColumn(name = "hr_user_id")
    private User hrUser;

    private String hrName;
    private String hrRemarks;
    private LocalDateTime hrAuthorizedAt;

    // Security Approval
    @Enumerated(EnumType.STRING)
    private ApprovalStatus securityStatus = ApprovalStatus.WAITING;

    @ManyToOne
    @JoinColumn(name = "security_user_id")
    private User securityUser;

    private String securityName;
    private String securityRemarks;
    private LocalDateTime securityAuthorizedAt;

    // ✅ NEW: Actual Out/In Time Tracking for Security
    @Column(name = "actual_out_time")
    private LocalDateTime actualOutTime;  // When employee actually left

    @Column(name = "actual_in_time")
    private LocalDateTime actualInTime;   // When employee actually returned

    @Column(name = "time_status")
    @Enumerated(EnumType.STRING)
    private TimeStatus timeStatus = TimeStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    private OverallStatus overallStatus = OverallStatus.PENDING_HOD;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED, WAITING
    }

    public enum OverallStatus {
        PENDING_HOD,
        PENDING_HR,
        PENDING_SECURITY,
        APPROVED,
        REJECTED
    }

    // ✅ NEW: Time Status Enum
    public enum TimeStatus {
        NOT_STARTED,    // Not yet out
        OUT,            // Employee has left
        RETURNED,       // Employee has returned
        OVERDUE         // Returned late
    }

    // ✅ UPDATE OVERALL STATUS
    public void updateOverallStatus() {
        // Check if ANYONE rejected (HOD or HR)
        if (hodStatus == ApprovalStatus.REJECTED || hrStatus == ApprovalStatus.REJECTED) {
            this.overallStatus = OverallStatus.REJECTED;
            return;
        }

        // Check if HOD OR HR approved
        if (hodStatus == ApprovalStatus.APPROVED || hrStatus == ApprovalStatus.APPROVED) {
            this.overallStatus = OverallStatus.APPROVED;
            return;
        }

        // Check if HOD is PENDING
        if (hodStatus == ApprovalStatus.PENDING) {
            this.overallStatus = OverallStatus.PENDING_HOD;
            return;
        }

        // Check if HR is PENDING or WAITING
        if (hrStatus == ApprovalStatus.PENDING || hrStatus == ApprovalStatus.WAITING) {
            this.overallStatus = OverallStatus.PENDING_HR;
            return;
        }

        this.overallStatus = OverallStatus.PENDING_HOD;
    }

    // ===== Helper Methods =====
    public boolean isAuthorized() {
        return this.overallStatus == OverallStatus.APPROVED;
    }

    public boolean isPending() {
        return this.overallStatus == OverallStatus.PENDING_HOD ||
                this.overallStatus == OverallStatus.PENDING_HR ||
                this.overallStatus == OverallStatus.PENDING_SECURITY;
    }

    public boolean isRejected() {
        return this.overallStatus == OverallStatus.REJECTED;
    }

    public String getAuthorizedBy() {
        if (hodStatus == ApprovalStatus.APPROVED) {
            return "HOD: " + (hodName != null ? hodName : "Unknown");
        }
        if (hrStatus == ApprovalStatus.APPROVED) {
            return "HR: " + (hrName != null ? hrName : "Unknown");
        }
        return "Not Authorized";
    }

    // ✅ NEW: Check if employee is currently out
    public boolean isCurrentlyOut() {
        return timeStatus == TimeStatus.OUT;
    }

    // ✅ NEW: Check if employee has returned
    public boolean hasReturned() {
        return timeStatus == TimeStatus.RETURNED || timeStatus == TimeStatus.OVERDUE;
    }

    // ✅ NEW: Check if employee is overdue
    public boolean isOverdue() {
        return timeStatus == TimeStatus.OVERDUE;
    }

    // ✅ NEW: Calculate duration of absence
    public String getDuration() {
        if (actualOutTime == null) return "Not started";
        if (actualInTime == null) {
            // Currently out - calculate from out time to now
            long minutes = java.time.Duration.between(actualOutTime, LocalDateTime.now()).toMinutes();
            return formatDuration(minutes);
        }
        long minutes = java.time.Duration.between(actualOutTime, actualInTime).toMinutes();
        return formatDuration(minutes);
    }

    private String formatDuration(long minutes) {
        if (minutes < 0) return "0m";
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (hours > 0) {
            return hours + "h " + mins + "m";
        }
        return mins + "m";
    }

    // ✅ NEW: Get status badge color
    public String getTimeStatusBadge() {
        switch (timeStatus) {
            case NOT_STARTED: return "secondary";
            case OUT: return "warning";
            case RETURNED: return "success";
            case OVERDUE: return "danger";
            default: return "secondary";
        }
    }

    // ✅ NEW: Get status label
    public String getTimeStatusLabel() {
        switch (timeStatus) {
            case NOT_STARTED: return "Not Started";
            case OUT: return "Out";
            case RETURNED: return "Returned";
            case OVERDUE: return "Overdue";
            default: return "Unknown";
        }
    }

    public String getRejectionReason() {
        if (hodStatus == ApprovalStatus.REJECTED && hodRemarks != null) {
            return hodRemarks;
        }
        if (hrStatus == ApprovalStatus.REJECTED && hrRemarks != null) {
            return hrRemarks;
        }
        return null;
    }

    public String getRejectedBy() {
        if (hodStatus == ApprovalStatus.REJECTED) {
            return "HOD: " + (hodName != null ? hodName : "Unknown");
        }
        if (hrStatus == ApprovalStatus.REJECTED) {
            return "HR: " + (hrName != null ? hrName : "Unknown");
        }
        return null;
    }
}