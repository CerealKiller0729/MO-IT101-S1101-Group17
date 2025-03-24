package com.mycompany.motorph;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SSS extends Calculation {

    private double sssDeduction; // SSS deduction amount
    private final Grosswage grosswage; // Gross wage object for calculation

    private static final String XLSX_FILE_PATH = "src/main/resources/SSSCont.xlsx"; // Path to the SSS contributions Excel file
    private static final List<SSSRecord> sssDeductionRecords; // List of SSS deduction records

    // Static block to load SSS deduction records when the class is loaded
    static {
        sssDeductionRecords = loadSssDeductions();
        if (sssDeductionRecords == null) {
            throw new RuntimeException("Failed to load SSS deductions.");
        }
    }

    /**
     * Constructor for SSS.
     * @param grosswage The Grosswage object containing the employee's gross wage.
     */
    public SSS(Grosswage grosswage) {
        this.grosswage = grosswage;
    }

    /**
     * Calculates the SSS deduction based on the employee's gross wage.
     * @return The SSS deduction amount.
     */
    @Override
    public double calculate() {
        double gross = grosswage.calculate();

        // Initialize SSS deduction to 0
        sssDeduction = 0.0;

        // Iterate through the SSS deduction records
        for (SSSRecord record : sssDeductionRecords) {
            double[] range = parseSssCompensationRange(record.getCompensationRange());

            // Check if the gross wage falls within the range
            if (gross >= range[0] && gross <= range[1]) {
                sssDeduction = record.getContribution();
                break;
            }
        }

        // If no range matches, apply the maximum contribution
        if (sssDeduction == 0.0) {
            double maxContribution = sssDeductionRecords.stream()
                    .mapToDouble(SSSRecord::getContribution)
                    .max()
                    .orElse(0.0);
            sssDeduction = maxContribution;
        }

        return sssDeduction;
    }

    /**
     * Loads SSS deduction records from an Excel file.
     * @return A list of SSSRecord objects.
     */
    private static List<SSSRecord> loadSssDeductions() {
        List<SSSRecord> deductionRecords = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(XLSX_FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            // Skip the header row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    String compensationRange = getCellValueAsString(row.getCell(0)); // Compensation range
                    double contribution = parseCellValueAsDouble(row.getCell(3)); // Contribution amount (Column D)

                    // Create a new SSSRecord object and add it to the list
                    deductionRecords.add(new SSSRecord(compensationRange, contribution));
                }
            }
        } catch (IOException e) {
            handleException(e);
        }

        return deductionRecords;
    }

    /**
     * Helper method to parse a cell's value as a double, even if it's stored as a string.
     */
    private static double parseCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return 0.0; // Return 0 if the cell is null
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty() || value.equals("-")) {
                    return 0.0; // Return 0 for empty cells or hyphens
                }
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid numeric format in cell: " + value);
                    return 0.0; // Return 0 if the string cannot be parsed as a number
                }
            default:
                return 0.0; // Return 0 for other cell types
        }
    }

    /**
     * Helper method to get the value of a cell as a String.
     * @param cell The cell to retrieve the value from.
     * @return The cell value as a String.
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * Parses the SSS compensation range string into a numeric range.
     * @param compensationRange The compensation range string (e.g., "Below 3,250").
     * @return An array containing the start and end values of the range.
     */
    private static double[] parseSssCompensationRange(String compensationRange) {
        compensationRange = compensationRange.trim(); // Remove extra spaces

        // Handle the "Below X" format
        if (compensationRange.startsWith("Below")) {
            String endValue = compensationRange.replace("Below", "").trim();
            double end = parseNumber(endValue);
            return new double[]{0, end};
        }

        // Handle the "Over" format
        if (compensationRange.contains("Over")) {
            String startValue = compensationRange.replace("Over", "").trim();
            double start = parseNumber(startValue);
            return new double[]{start, Double.MAX_VALUE};
        }

        // Handle the "X - Y" format
        if (compensationRange.contains("-")) {
            String[] rangeParts = compensationRange.split("-");
            if (rangeParts.length == 2) {
                try {
                    double start = parseNumber(rangeParts[0].trim());
                    double end = parseNumber(rangeParts[1].trim());
                    return new double[]{start, end};
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid numeric format in compensation range: " + compensationRange, e);
                }
            }
        }

        // Handle single numeric values (e.g., "3250.0")
        try {
            double value = parseNumber(compensationRange);
            return new double[]{value, value}; // Treat single value as a range with the same start and end
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid compensation range format: " + compensationRange, e);
        }
    }

    /**
     * Parses a number from a string, removing commas and other non-numeric characters.
     * @param numberString The string to parse.
     * @return The parsed number as a double.
     */
    private static double parseNumber(String numberString) {
        // Remove commas and other non-numeric characters
        numberString = numberString.replace(",", "").trim();
        return Double.parseDouble(numberString);
    }

    /**
     * Handles exceptions by printing the stack trace.
     * @param e The exception to handle.
     */
    private static void handleException(Exception e) {
        e.printStackTrace();
    }

    /**
     * Returns the SSS deduction amount.
     * @return The SSS deduction amount.
     */
    public double getSssDeduction() {
        return sssDeduction;
    }
}