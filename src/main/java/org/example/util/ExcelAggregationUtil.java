package org.example.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelAggregationUtil {

    private ExcelAggregationUtil() {
    }

    public static int mergeFirstSheet(List<Path> sourceFiles, Path targetFile) {
        if (sourceFiles.isEmpty()) {
            throw new IllegalArgumentException("没有可汇总的文件");
        }

        try (Workbook targetWorkbook = new XSSFWorkbook()) {
            Sheet targetSheet = targetWorkbook.createSheet("汇总");
            int targetRowIndex = 0;
            boolean headerCopied = false;

            for (Path source : sourceFiles) {
                if (!Files.exists(source)) {
                    continue;
                }
                try (Workbook workbook = new XSSFWorkbook(Files.newInputStream(source))) {
                    Sheet sheet = workbook.getSheetAt(0);
                    if (sheet == null) {
                        continue;
                    }
                    int firstRow = sheet.getFirstRowNum();
                    int lastRow = sheet.getLastRowNum();
                    for (int i = firstRow; i <= lastRow; i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) {
                            continue;
                        }
                        if (i == firstRow && headerCopied) {
                            // skip header
                            continue;
                        }
                        Row newRow = targetSheet.createRow(targetRowIndex++);
                        copyRow(row, newRow);
                    }
                    headerCopied = true;
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream(targetFile.toFile())) {
                targetWorkbook.write(outputStream);
            }
            return targetRowIndex;
        } catch (IOException e) {
            throw new IllegalStateException("合并 Excel 失败", e);
        }
    }

    private static void copyRow(Row source, Row target) {
        for (int cellIndex = 0; cellIndex < source.getLastCellNum(); cellIndex++) {
            Cell sourceCell = source.getCell(cellIndex);
            Cell targetCell = target.createCell(cellIndex);
            if (sourceCell == null) {
                continue;
            }
            switch (sourceCell.getCellType()) {
                case STRING -> targetCell.setCellValue(sourceCell.getStringCellValue());
                case NUMERIC -> targetCell.setCellValue(sourceCell.getNumericCellValue());
                case BOOLEAN -> targetCell.setCellValue(sourceCell.getBooleanCellValue());
                case FORMULA -> targetCell.setCellFormula(sourceCell.getCellFormula());
                default -> targetCell.setCellValue(sourceCell.toString());
            }
        }
    }
}

