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
    // Default path for the Excel file containing employee data
    private static String filePath = "src/main/resources/EmployeeData.xlsx";
    
    // List to store employee objects loaded from the Excel file
    private static final List<Employee> employees = loadEmployees();

    /**
     * Loads employee data from the Excel file
     * @return List of Employee objects
     */
    private static List<Employee> loadEmployees() {
        // List to hold employee objects
        List<Employee> employeeList = new ArrayList<>();

        // Try-with-resources to automatically close resources after use
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Get the first sheet in the workbook
            Sheet sheet = workbook.getSheetAt(0);

            // Iterate over each row in the sheet
            for (Row row : sheet) {
                // Skip header row
                if (row.getRowNum() == 0) continue;

                // List to hold the row data
                List<String> rowData = new ArrayList<>();

                // Iterate over each cell in the row
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        // Handle string cells
                        case STRING -> rowData.add(cell.getStringCellValue().trim());
                        
                        // Handle numeric cells (including date and non-date numbers)
                        case NUMERIC -> {
                            if (DateUtil.isCellDateFormatted(cell)) {
                                // Handle date-formatted cells
                                Date date = cell.getDateCellValue();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                                rowData.add(dateFormat.format(date));
                            } else {
                                // Handle numeric cells (convert to string to avoid scientific notation)
                                rowData.add(String.valueOf((long) cell.getNumericCellValue())); // Casting to long for proper representation
                            }
                        }

                        // Handle other cell types by adding an empty string
                        default -> rowData.add("");
                    }
                }

                // If the row contains enough data (at least 19 cells), create an Employee object
                if (rowData.size() >= 19) {
                    employeeList.add(new Employee(rowData.toArray(new String[0])));
                }
            }
        } catch (IOException e) {
            // Error handling in case of issues with reading the file
            System.err.println("Error loading employee data: " + e.getMessage());
        }

        // Return the list of employees loaded from the file
        return employeeList;
    }

    /**
     * Gets an unmodifiable list of all employees
     * @return List of Employee objects
     */
    public static List<Employee> getEmployeeModelList() {
        // Return an unmodifiable list to ensure data integrity
        return Collections.unmodifiableList(employees);
    }

    /**
     * Finds an employee by ID
     * @param employeeId The employee ID to search for
     * @return Employee object if found, null otherwise
     */
    public static Employee getEmployeeById(String employeeId) {
        // Stream through the employee list and find the employee by ID
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
        // Update the file path to a new value
        filePath = newFilePath;
    }
}
