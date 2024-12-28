package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportServiceImpl implements ExcelExportService {
    public byte[] generateExcelFile(List<List<String>> data, List<String> headers) throws IOException {
        // Membuat workbook baru
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Report");

        // Membuat header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        // Mengisi data
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            List<String> rowData = data.get(i);
            for (int j = 0; j < rowData.size(); j++) {
                row.createCell(j).setCellValue(rowData.get(j));
            }
        }

        // Menyimpan ke output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}
