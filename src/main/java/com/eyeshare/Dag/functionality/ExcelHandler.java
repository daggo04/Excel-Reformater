package com.eyeshare.Dag.functionality;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Created by Dag O.B.H on 2023.18.04.
 * <p>This class is responsible for handling the Excel files and contains methods for formatting and data transfer.</p>
 */
public class ExcelHandler {
    private Workbook source;
    private Workbook template;
    private Workbook output;


    // Constructors
    /**
     * Constructor for a new ExcelHandler object.
     * @param filePath The path to the file to be loaded.
     * @throws IOException if the file cannot be found.
     */
    public ExcelHandler(String filePath) throws IOException {
        this.source = new XSSFWorkbook(new FileInputStream(filePath));
        this.template = null;
    }

    /**
     * Constructor for a new ExcelHandler object.
     * @param sourceFilePath The path to the source file to be loaded.
     * @param templateFilePath The path to the template file to be loaded.
     * @throws IOException if the file cannot be found.
     */
    public ExcelHandler(String sourceFilePath, String templateFilePath) throws IOException {
        try {
            Path sourcePath = Paths.get(sourceFilePath);
            Path templatePath = Paths.get(System.getProperty("user.home"), ".Excel_Reformatter_Resources", templateFilePath);

            this.source = WorkbookFactory.create(Files.newInputStream(sourcePath));
            this.template = WorkbookFactory.create(Files.newInputStream(templatePath));

            // Initialize the output workbook as a copy of the template workbook
            ByteArrayOutputStream templateBytes = new ByteArrayOutputStream();
            this.template.write(templateBytes);
            ByteArrayInputStream outputBytes = new ByteArrayInputStream(templateBytes.toByteArray());
            this.output = WorkbookFactory.create(outputBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor for a new ExcelHandler object.
     * @param source The source workbook to be loaded.
     * @param template The template workbook to be loaded.
     */
    public ExcelHandler(Workbook source, Workbook template) {
        this.source = source;
        this.template = template;
    }

    /**
     * Constructor for a new ExcelHandler object.
     * @param source The source workbook to be loaded.
     */
    public ExcelHandler(Workbook source) {
        this.source = source;
        this.template = null;
    }


    public void saveOutputWorkbook(String outputFilePath) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(outputFilePath)) {
            output.write(fileOut);
        }
        closeWorkbooks();
    }
    
    public void copyRows(int srcSheet, int dstSheet, int startRow, int endRow) {
        Sheet sourceSheet = source.getSheetAt(srcSheet);
        Sheet targetSheet = output.getSheetAt(dstSheet);
    
        for (int i = startRow; i <= endRow; i++) {
            Row sourceRow = sourceSheet.getRow(i);
            Row targetRow = targetSheet.createRow(i);
            copyRow(sourceRow, targetRow);
        }
    }
    
    public void copyColumn(int srcSheet, int srcCol, int dstSheet, int dstCol, int startRow) {
        Sheet sourceSheet = source.getSheetAt(srcSheet);
        Sheet targetSheet = output.getSheetAt(dstSheet);
        int lastRowNum = sourceSheet.getLastRowNum();
    
        for (int i = startRow; i <= lastRowNum; i++) {
            Row sourceRow = sourceSheet.getRow(i);
            Row targetRow = targetSheet.getRow(i);
    
            if (sourceRow != null) {
                Cell sourceCell = sourceRow.getCell(srcCol);
                if (targetRow == null) {
                    targetRow = targetSheet.createRow(i);
                }
                Cell targetCell = targetRow.createCell(dstCol);
                copyCell(sourceCell, targetCell);
            }
        }
    }

    public void copySplitRow(int srcSheet, int dstSheet, int startRow, Map<Integer, Integer> colMap, boolean includeHeaders, int headerCol) {
        Sheet sourceSheet = source.getSheetAt(srcSheet);
        Sheet targetSheet = output.getSheetAt(dstSheet);

        int lastSourceRow = sourceSheet.getLastRowNum();
        int targetRowIdx = startRow;

        HashMap<Integer, List<Integer>> invertedColMap = invertcolMap(colMap);
        System.out.println(invertedColMap);
        
        int splits = 1;
        int keyToSplit = 0;
        for (int key : invertedColMap.keySet()) {
            if (invertedColMap.get(key).size() > splits) {
                splits = invertedColMap.get(key).size();
                keyToSplit = key;
            }
        }

        for (int i = startRow; i <= lastSourceRow; i++) {
            Row sourceRow = sourceSheet.getRow(i);
            if (areNextRowsEmpty(sourceSheet, i, 10)) break;
            if (sourceRow != null) {
                for (int j = 0; j < splits; j++) {
                    // Break loop if it exceeds the last row index or if the next rows are empty

                    Row targetRow = targetSheet.createRow(targetRowIdx++);
                    for (int key : invertedColMap.keySet()) {
                        Cell sourceCell;
                        if (key == keyToSplit) {
                            sourceCell = sourceRow.getCell(invertedColMap.get(key).get(Math.min(j, invertedColMap.get(key).size() - 1)));
                        } else {
                            sourceCell = sourceRow.getCell(invertedColMap.get(key).get(0));
                        }
                        Cell targetCell = targetRow.createCell(key);
                        copyCell(sourceCell, targetCell);

                        // Copy headers
                        if (includeHeaders && i <= lastSourceRow) {
                            Cell headerCell = sourceSheet.getRow(0).getCell(invertedColMap.get(key).get(Math.min(j, invertedColMap.get(key).size() - 1)));
                            Cell targetHeaderCell = targetRow.createCell(headerCol);
                            copyCell(headerCell, targetHeaderCell);
                        }
                    }
                }
            }
        }
    }

    //Helper methods
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }


    private boolean areNextRowsEmpty(Sheet sheet, int startRow, int n) {
        for (int i = startRow; i < startRow + n && i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (!isRowEmpty(row)) {
                return false;
            }
        }
        return true;
    }


    private void copyCell(Cell sourceCell, Cell destinationCell) {
        if (sourceCell == null) {
            destinationCell.setBlank();
            return;
        }
    
        CellType cellType = sourceCell.getCellType();
        
        Workbook destinationWorkbook = destinationCell.getSheet().getWorkbook();
        CellStyle newStyle = destinationWorkbook.createCellStyle();
        newStyle.cloneStyleFrom(sourceCell.getCellStyle());
        newStyle.setDataFormat(destinationWorkbook.createDataFormat().getFormat(sourceCell.getCellStyle().getDataFormatString()));
        
    
        switch (cellType) {
            case STRING:
                destinationCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                destinationCell.setCellStyle(newStyle);
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    destinationCell.setCellValue(sourceCell.getDateCellValue());
                } else {
                    destinationCell.setCellValue(sourceCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                destinationCell.setCellStyle(newStyle);
                destinationCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                destinationCell.setCellStyle(newStyle);
                destinationCell.setCellFormula(sourceCell.getCellFormula());
                break;
            case BLANK:
                destinationCell.setCellStyle(newStyle);
                destinationCell.setBlank();
                break;
            default:
                break;
        }
    }
    


    private void copyRow(Row sourceRow, Row destinationRow) {
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            Cell sourceCell = sourceRow.getCell(i);
            if (sourceCell == null) continue;
            Cell destinationCell = destinationRow.createCell(i);
            copyCell(sourceCell, destinationCell);
        }
    }

    private HashMap<Integer, List<Integer>> invertcolMap(Map<Integer, Integer> colMap) {
        HashMap<Integer, List<Integer>> invertedMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : colMap.entrySet()) {
            int srcCol = entry.getKey();
            int dstCol = entry.getValue();
            invertedMap.putIfAbsent(dstCol, new ArrayList<>());
            invertedMap.get(dstCol).add(srcCol);
        }
        return invertedMap;
    }


    private void closeWorkbooks() {
        if (source != null) {
            try {
                source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (template != null) {
            try {
                template.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}