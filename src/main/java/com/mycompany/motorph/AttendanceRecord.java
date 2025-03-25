package com.mycompany.motorph;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
/**
 *
 * @author angeliquerivera
 */

public class AttendanceRecord {

    private String name;
    private String id;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private static final String XLSX_FILE_PATH = "src/main/resources/AttendanceRecord.xlsx";
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // Updated to match Excel time format
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Updated to match Excel date format

    // Constructor
    public AttendanceRecord(String name, String id, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.name = name;
        this.id = id;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    // Constructor that accepts a String array
    public AttendanceRecord(String[] data) {
        if (data.length < 6) {
            throw new IllegalArgumentException("Insufficient data to create AttendanceRecord");
        }
        this.id = data[0];
        this.name = data[1] + " " + data[2].trim(); // Combine first and last name
        this.date = LocalDate.parse(data[3], dateFormatter);
        this.timeIn = LocalTime.parse(data[4], timeFormatter);
        this.timeOut = LocalTime.parse(data[5], timeFormatter);
    }

    // Default constructor
    public AttendanceRecord() {}

    // Loads attendance from an Excel file
    public static void loadAttendanceFromExcel(String filePath) {
        try {
            attendanceRecords = loadAttendance(filePath);
            System.out.println("Loaded " + attendanceRecords.size() + " attendance records.");
        } catch (IOException e) {
            System.err.println("Error loading attendance records: " + e.getMessage());
        }
    }

    // Loads attendance from an Excel file and returns a list of AttendanceRecord objects
    public static ArrayList<AttendanceRecord> loadAttendance(String filePath) throws IOException {
        ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            // Skip the header row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    // Normalize the employee ID by removing the ".0" suffix
                    String id = getCellValueAsString(row.getCell(0)).replace(".0", "");
                    String name = getCellValueAsString(row.getCell(1));
                    String surname = getCellValueAsString(row.getCell(2)).trim();

                    LocalDate date = parseDate(row.getCell(3));
                    LocalTime timeIn = parseTime(row.getCell(4));
                    LocalTime timeOut = parseTime(row.getCell(5));

                    // Skip records with missing or invalid time values
                    if (timeIn == null || timeOut == null) {
                        System.out.println("Skipping record with missing time values: " + id);
                        continue;
                    }

                    // Create a new AttendanceRecord and add it to the list
                    attendanceRecords.add(new AttendanceRecord(name + " " + surname, id, date, timeIn, timeOut));
                }
            }
        }

        return attendanceRecords;
    }

    // Parses a date from a cell
    private static LocalDate parseDate(Cell cell) {
        if (cell == null) {
            System.err.println("Date cell is null.");
            return null;
        }
        try {
            String cellValue = getCellValueAsString(cell);

            if (cell.getCellType() == CellType.NUMERIC) {
                // Convert Excel numeric date to LocalDate
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                String dateString = cellValue.trim();
                // Extract only the date part (first 10 characters) from "yyyy-MM-dd HH:mm:ss"
                String dateOnly = dateString.substring(0, 10);
                return LocalDate.parse(dateOnly, dateFormatter);
            } else {
                System.err.println("Unsupported cell type for date: " + cell.getCellType());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error parsing date: " + e.getMessage());
            return null;
        }
    }

    // Parses a time from a cell
    private static LocalTime parseTime(Cell cell) {
        if (cell == null) {
            System.err.println("Time cell is null.");
            return null;
        }
        try {
            String cellValue = getCellValueAsString(cell);

            if (cell.getCellType() == CellType.NUMERIC) {
                // Convert Excel numeric time to LocalTime
                double numericValue = cell.getNumericCellValue();
                int hours = (int) (numericValue * 24);
                int minutes = (int) ((numericValue * 24 * 60) % 60);
                int seconds = (int) ((numericValue * 24 * 60 * 60) % 60);
                return LocalTime.of(hours, minutes, seconds);
            } else if (cell.getCellType() == CellType.STRING) {
                String timeString = cellValue.trim();
                if (timeString.isEmpty()) {
                    System.err.println("Time string is empty.");
                    return null;
                }
                return LocalTime.parse(timeString, timeFormatter);
            } else {
                System.err.println("Unsupported cell type for time: " + cell.getCellType());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error parsing time: " + e.getMessage());
            return null;
        }
    }

    // Gets the value of a cell as a string
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    // Calculates hours worked for a single attendance record
    public double calculateHoursWorked() {
        if (timeIn == null || timeOut == null) {
            System.err.println("TimeIn or TimeOut is null for record: " + id);
            return 0.0;
        }
        Duration duration;
        if (timeOut.isBefore(timeIn)) {
            // If timeOut is before timeIn, assume the employee worked past midnight
            duration = Duration.between(timeIn, timeOut.plusHours(24));
        } else {
            duration = Duration.between(timeIn, timeOut);
        }
        return duration.toHours() + (duration.toMinutes() % 60) / 60.0;
    }

    // Calculates total hours worked for a specific employee in a given month and year
    public static double calculateTotalHours(int year, int month, String employeeID, boolean isFirstHalf) {
        double totalHours = 0;

        for (AttendanceRecord record : attendanceRecords) {
            // Check if the record matches the employee ID, year, and month
            if (record.getId().equals(employeeID)) {
                if (record.getDate().getYear() == year) {
                    if (record.getDate().getMonthValue() == month) {
                        LocalDate recordDate = record.getDate();
                        int dayOfMonth = recordDate.getDayOfMonth();

                        // Check if the record is in the first or second half of the month
                        if ((isFirstHalf && dayOfMonth <= 15) || (!isFirstHalf && dayOfMonth > 15)) {
                            double hoursWorked = record.calculateHoursWorked();
                            totalHours += hoursWorked;
                        }
                    }
                }
            }
        }

        return totalHours;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTimeIn() {
        return timeIn;
    }

    public LocalTime getTimeOut() {
        return timeOut;
    }

    public static ArrayList<AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }
}