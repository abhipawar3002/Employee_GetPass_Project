package com.example.Employee_OutPass_Project.Service;

import com.example.Employee_OutPass_Project.Entity.OutPass;
import com.example.Employee_OutPass_Project.Entity.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ===== EXPORT ALL =====
    public ByteArrayInputStream exportOutPassesToExcel(List<OutPass> outPasses, String reportTitle, User user) {
        return exportOutPassesToExcel(outPasses, reportTitle, user, null, null, null);
    }

    // ===== EXPORT WITH FILTERS =====
    public ByteArrayInputStream exportOutPassesToExcel(List<OutPass> outPasses, String reportTitle,
                                                       User user, LocalDate fromDate,
                                                       LocalDate toDate, String status) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("OutPass Report");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle filterStyle = createFilterStyle(workbook);

            int rowNum = 0;

            // ===== TITLE ROW =====
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reportTitle);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 12));

            // ===== FILTER INFO ROW =====
            rowNum++;
            Row filterRow = sheet.createRow(rowNum++);
            StringBuilder filterInfo = new StringBuilder("Filters: ");
            if (fromDate != null) filterInfo.append("From: ").append(fromDate.format(DATE_FORMATTER)).append(" ");
            if (toDate != null) filterInfo.append("To: ").append(toDate.format(DATE_FORMATTER)).append(" ");
            if (status != null && !status.isEmpty()) filterInfo.append("Status: ").append(status).append(" ");
            filterInfo.append("| Total: ").append(outPasses.size());

            Cell filterCell = filterRow.createCell(0);
            filterCell.setCellValue(filterInfo.toString());
            filterCell.setCellStyle(filterStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(2, 2, 0, 12));

            // ===== GENERATED DATE ROW =====
            rowNum++;
            Row dateRow = sheet.createRow(rowNum++);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generated on: " + LocalDateTime.now().format(DATE_TIME_FORMATTER) + " | By: " + (user != null ? user.getFullName() : "System"));
            dateCell.setCellStyle(filterStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(3, 3, 0, 12));

            // ===== HEADER ROW =====
            rowNum++;
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {
                    "ID", "Employee Name", "Employee ID", "Department", "Date",
                    "Out Time", "Expected In Time", "Actual Out Time", "Actual In Time",
                    "Time Status", "Reason", "Status", "Authorized By"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 15 * 256);
            }
            // Set wider columns for reason
            sheet.setColumnWidth(10, 30 * 256);

            // ===== DATA ROWS =====
            for (OutPass p : outPasses) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getEmployeeName() != null ? p.getEmployeeName() : "");
                row.createCell(2).setCellValue(p.getEmployeeId() != null ? p.getEmployeeId() : "");
                row.createCell(3).setCellValue(p.getDepartment() != null ? p.getDepartment().getName() : "N/A");
                row.createCell(4).setCellValue(p.getPassDate() != null ? p.getPassDate().format(DATE_FORMATTER) : "");
                row.createCell(5).setCellValue(p.getOutTime() != null ? p.getOutTime().format(TIME_FORMATTER) : "");
                row.createCell(6).setCellValue(p.getExpectedInTime() != null ? p.getExpectedInTime().format(TIME_FORMATTER) : "");

                // Actual Out Time
                row.createCell(7).setCellValue(p.getActualOutTime() != null ?
                        p.getActualOutTime().format(DATE_TIME_FORMATTER) : "Not yet");

                // Actual In Time
                row.createCell(8).setCellValue(p.getActualInTime() != null ?
                        p.getActualInTime().format(DATE_TIME_FORMATTER) : "Not yet");

                row.createCell(9).setCellValue(p.getTimeStatus() != null ? p.getTimeStatusLabel() : "Not Started");
                row.createCell(10).setCellValue(p.getReason() != null ? p.getReason() : "");
                row.createCell(11).setCellValue(p.isAuthorized() ? "Authorized" : (p.isRejected() ? "Rejected" : "Pending"));
                row.createCell(12).setCellValue(p.getAuthorizedBy() != null ? p.getAuthorizedBy() : "-");

                // Apply data style
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // ===== SUMMARY SECTION =====
            rowNum++;
            Row summaryRow = sheet.createRow(rowNum++);
            Cell summaryCell = summaryRow.createCell(0);

            long authorized = outPasses.stream().filter(OutPass::isAuthorized).count();
            long rejected = outPasses.stream().filter(OutPass::isRejected).count();
            long pending = outPasses.stream().filter(OutPass::isPending).count();
            long outCount = outPasses.stream().filter(p -> p.isCurrentlyOut()).count();
            long returned = outPasses.stream().filter(p -> p.hasReturned()).count();

            String summary = "SUMMARY: Total: " + outPasses.size() +
                    " | Authorized: " + authorized +
                    " | Rejected: " + rejected +
                    " | Pending: " + pending +
                    " | Currently Out: " + outCount +
                    " | Returned: " + returned;

            summaryCell.setCellValue(summary);
            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryFont.setFontHeightInPoints((short) 11);
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summaryCell.setCellStyle(summaryStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 12));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== STYLE METHODS =====
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createFilterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}