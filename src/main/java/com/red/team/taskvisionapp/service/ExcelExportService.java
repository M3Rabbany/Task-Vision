package com.red.team.taskvisionapp.service;

import java.io.IOException;
import java.util.List;

public interface ExcelExportService {
    byte[] generateExcelFile(List<List<String>> data, List<String> headers) throws IOException;
}
