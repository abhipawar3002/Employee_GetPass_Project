package com.example.Employee_OutPass_Project.Controller;

import com.example.Employee_OutPass_Project.Entity.OutPass;
import com.example.Employee_OutPass_Project.Entity.User;
import com.example.Employee_OutPass_Project.Entity.User.Role;
import com.example.Employee_OutPass_Project.Service.ExcelExportService;
import com.example.Employee_OutPass_Project.Service.OutPassService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/hr")
public class HRController {

    private final OutPassService outPassService;
    private final ExcelExportService excelExportService;

    public HRController(OutPassService outPassService, ExcelExportService excelExportService) {
        this.outPassService = outPassService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String tab,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                            @RequestParam(required = false) String status,
                            HttpSession session,
                            Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null || user.getRole() != Role.HR) {
            return "redirect:/login";
        }

        try {
            List<OutPass> allPasses = outPassService.getAllOutPasses();

            // Apply filters
            if (fromDate != null) {
                allPasses = allPasses.stream()
                        .filter(p -> p.getPassDate() != null && !p.getPassDate().isBefore(fromDate))
                        .collect(Collectors.toList());
            }
            if (toDate != null) {
                allPasses = allPasses.stream()
                        .filter(p -> p.getPassDate() != null && !p.getPassDate().isAfter(toDate))
                        .collect(Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                if (status.equals("APPROVED")) {
                    allPasses = allPasses.stream().filter(OutPass::isAuthorized).collect(Collectors.toList());
                } else if (status.equals("REJECTED")) {
                    allPasses = allPasses.stream().filter(OutPass::isRejected).collect(Collectors.toList());
                } else if (status.equals("PENDING_HR") || status.equals("PENDING")) {
                    allPasses = allPasses.stream().filter(OutPass::isPending).collect(Collectors.toList());
                }
            }

            long totalCount = allPasses != null ? allPasses.size() : 0;
            long approvedCount = allPasses != null ?
                    allPasses.stream().filter(p -> p.isAuthorized()).count() : 0;
            long rejectedCount = allPasses != null ?
                    allPasses.stream().filter(p -> p.isRejected()).count() : 0;
            long pendingCount = allPasses != null ?
                    allPasses.stream().filter(p -> p.isPending()).count() : 0;

            model.addAttribute("loggedInUser", user);
            model.addAttribute("allPasses", allPasses != null ? allPasses : List.of());
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("pendingCount", pendingCount);

            model.addAttribute("filterFromDate", fromDate);
            model.addAttribute("filterToDate", toDate);
            model.addAttribute("filterStatus", status);

            if (tab != null && !tab.isEmpty()) {
                model.addAttribute("activeTab", tab);
            } else {
                model.addAttribute("activeTab", "dashboard");
            }

            return "dashboard/hr";

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error";
        }
    }

    // ===== EXCEL EXPORT =====
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null || (user.getRole() != Role.HR && user.getRole() != Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<OutPass> allPasses = outPassService.getAllOutPasses();

            // Apply filters
            if (fromDate != null) {
                allPasses = allPasses.stream()
                        .filter(p -> p.getPassDate() != null && !p.getPassDate().isBefore(fromDate))
                        .collect(Collectors.toList());
            }
            if (toDate != null) {
                allPasses = allPasses.stream()
                        .filter(p -> p.getPassDate() != null && !p.getPassDate().isAfter(toDate))
                        .collect(Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                if (status.equals("APPROVED")) {
                    allPasses = allPasses.stream().filter(OutPass::isAuthorized).collect(Collectors.toList());
                } else if (status.equals("REJECTED")) {
                    allPasses = allPasses.stream().filter(OutPass::isRejected).collect(Collectors.toList());
                } else if (status.equals("PENDING_HR") || status.equals("PENDING")) {
                    allPasses = allPasses.stream().filter(OutPass::isPending).collect(Collectors.toList());
                }
            }

            if (allPasses == null || allPasses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            StringBuilder title = new StringBuilder("HR OutPass Report");
            if (fromDate != null) title.append(" - From: ").append(fromDate);
            if (toDate != null) title.append(" - To: ").append(toDate);
            if (status != null && !status.isEmpty()) title.append(" - Status: ").append(status);

            ByteArrayInputStream excelStream = excelExportService.exportOutPassesToExcel(
                    allPasses,
                    title.toString(),
                    user,
                    fromDate,
                    toDate,
                    status
            );

            if (excelStream == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            byte[] excelBytes = excelStream.readAllBytes();
            HttpHeaders headers = new HttpHeaders();

            String filename = "HR_OutPass_Report_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            headers.add("Content-Disposition", "attachment; filename=" + filename);
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok().headers(headers).body(excelBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/authorize")
    public String authorize(@RequestParam Long passId,
                            @RequestParam(required = false) String remarks,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.HR) {
            return "redirect:/login";
        }

        try {
            OutPass authorized = outPassService.authorizeHR(passId, user, remarks != null ? remarks : "Approved by HR");
            redirectAttributes.addFlashAttribute("success", "✅ Out Pass #" + passId + " authorized successfully!");
            return "redirect:/hr/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
            return "redirect:/hr/dashboard";
        }
    }

    @PostMapping("/reject")
    public String reject(@RequestParam Long passId,
                         @RequestParam String remarks,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.HR) {
            return "redirect:/login";
        }

        try {
            if (remarks == null || remarks.trim().isEmpty()) {
                throw new RuntimeException("Please provide a reason for rejection.");
            }

            outPassService.rejectHR(passId, user, remarks);
            redirectAttributes.addFlashAttribute("success", "❌ Out Pass #" + passId + " rejected successfully!");
            return "redirect:/hr/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
            return "redirect:/hr/dashboard";
        }
    }
}