package com.mycompany.motorph;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date; 
import java.text.SimpleDateFormat;
import java.util.List;

public class EmployeeModelFromFile {
    private static String filePath = "src/main/resources/EmployeeData.xlsx";
    private static final List<Employee> employees = loadEmployees();

    /**
     * Loads employee data from the Excel file
     * @return List of Employee objects
     */
    private static List<Employee> loadEmployees() {
        List<Employee> employeeList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                         case STRING -> rowData.add(cell.getStringCellValue().trim());
                        case NUMERIC -> {
                              if (DateUtil.isCellDateFormatted(cell)) {
                                Date date = cell.getDateCellValue();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy"); // Format as mm/dd/yyyy
                                rowData.add(dateFormat.format(date));
                            } else {
                                // Read as string to avoid scientific notation
                                rowData.add(String.valueOf((long) cell.getNumericCellValue())); // Cast to long to avoid decimals
                            }
                        }
                        default -> rowData.add("");
                    }
                }

                if (rowData.size() >= 19) {
                    employeeList.add(new Employee(rowData.toArray(new String[0])));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading employee data: " + e.getMessage());
        }

        return employeeList;
    }

    /**
     * Gets an unmodifiable list of all employees
     * @return List of Employee objects
     */
    public static List<Employee> getEmployeeModelList() {
        return Collections.unmodifiableList(employees);
    }

    /**
     * Finds an employee by ID
     * @param employeeId The employee ID to search for
     * @return Employee object if found, null otherwise
     */
    public static Employee getEmployeeById(String employeeId) {
        return employees.stream()
                .filter(e -> e.getEmployeeNumber().equals(employeeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates the file path for the employee data
     * @param newFilePath New path to the employee data file
     */
    public static void setFilePath(String newFilePath) {
        filePath = newFilePath;
    }
}
