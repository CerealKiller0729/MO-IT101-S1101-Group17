/*
 * Class for loading and managing employee data from an Excel file.
 */
package com.mycompany.motorph;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author angeliquerivera
 */

public class EmployeeModelFromFile {
    // Path to the Excel file containing employee data
    private static String XLSX_FILE_PATH = "src/main/resources/EmployeeData.xlsx";
    private static final List<Employee> employees;

    // Static block to initialize the list of employees when the class is loaded
    static {
        employees = loadEmployees();
    }

    /**
     * Loads employee data from the Excel file.
     * @return A list of Employee objects.
     */
    public static List<Employee> loadEmployees() {
        // Print the current working directory for debugging
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        // Initialize a new list to store employee data
        List<Employee> employees = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(XLSX_FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            // Skip the header row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    List<String> employeeData = new ArrayList<>();

                    // Iterate through each cell in the row
                    for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                        Cell cell = row.getCell(cellIndex);
                        employeeData.add((cell != null) ? cell.toString() : ""); // Convert cell to string
                    }

                    // Ensure the row has the expected number of fields (19)
                    if (employeeData.size() >= 19) {
                        Employee employee = new Employee(employeeData.toArray(new String[0]));
                        employees.add(employee);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading employee data: " + e.getMessage());
        }

        return employees; // Return the list of loaded Employee objects
    }

    /**
     * Returns the list of employees.
     * @return The list of Employee objects.
     */
    public static List<Employee> getEmployeeModelList() {
        return employees;
    }

    /**
     * Sets the path to the Excel file containing employee data.
     * @param aXLSX_FILE_PATH The new path to the Excel file.
     */
    public static void setXLSX_FILE_PATH(String aXLSX_FILE_PATH) {
        XLSX_FILE_PATH = aXLSX_FILE_PATH;
    }
}