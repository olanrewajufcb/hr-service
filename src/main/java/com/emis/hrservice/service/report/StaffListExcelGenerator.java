package com.emis.hrservice.service.report;

import com.emis.hrservice.domain.StaffListReportRow;
import com.emis.hrservice.exceptions.ReportGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class StaffListExcelGenerator {

    public byte[] generate(
            String schoolName,
            String academicYear,
            List<StaffListReportRow> staff
    ) {

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Staff List");

            int rowIdx = 0;

            // ===== Header Info =====
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue("STAFF LIST REPORT");

            Row meta1 = sheet.createRow(rowIdx++);
            meta1.createCell(0).setCellValue("School:");
            meta1.createCell(1).setCellValue(schoolName);

            Row meta2 = sheet.createRow(rowIdx++);
            meta2.createCell(0).setCellValue("Academic Year:");
            meta2.createCell(1).setCellValue(academicYear);

            Row meta3 = sheet.createRow(rowIdx++);
            meta3.createCell(0).setCellValue("Generated At:");
            meta3.createCell(1).setCellValue(LocalDateTime.now().toString());

            rowIdx++; // blank row

            // ===== Table Header =====
            Row header = sheet.createRow(rowIdx++);
            createHeader(header,
                    "Staff Code",
                    "Full Name",
                    "Role",
                    "Category",
                    "Employment Type",
                    "Status"
            );

            // ===== Data Rows =====
            for (StaffListReportRow row : staff) {
                Row data = sheet.createRow(rowIdx++);
                data.createCell(0).setCellValue(row.getStaffCode());
                data.createCell(1).setCellValue(row.getFullName());
                data.createCell(2).setCellValue(row.getStaffRole());
                data.createCell(3).setCellValue(row.getStaffCategory());
                data.createCell(4).setCellValue(row.getEmploymentType());
                data.createCell(5).setCellValue(row.getStatus());
            }

            // Autosize columns
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            workbook.dispose();

            return out.toByteArray();

        } catch (Exception ex) {
            throw new ReportGenerationException("Failed to generate staff list Excel", ex);
        }
    }

    private void createHeader(Row row, String... headers) {
        CellStyle style = row.getSheet().getWorkbook().createCellStyle();
        Font font = row.getSheet().getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }
}