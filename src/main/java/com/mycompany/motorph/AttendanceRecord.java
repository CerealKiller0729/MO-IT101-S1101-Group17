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

public class AttendanceRecord {
    // Attributes to store employee attendance details
    private String name;
    private String id;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;

    // Constants for file path and formatting
    private static final String XLSX_FILE_PATH = "src/main/resources/AttendanceRecord.xlsx";
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Collection to store attendance records
    public static ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();

    // Constructor to initialize an AttendanceRecord object
    public AttendanceRecord(String name, String id, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.name = name;
        this.id = id;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    // Constructor to initialize an AttendanceRecord from an array of data
    public AttendanceRecord(String[] data) {
        if (data.length < 6) {
            throw new IllegalArgumentException("Insufficient data to create AttendanceRecord");
        }
        this.id = data[0];
        this.name = data[1] + " " + data[2].trim();
        this.date = LocalDate.parse(data[3], dateFormatter);
        this.timeIn = LocalTime.parse(data[4], timeFormatter);
        this.timeOut = LocalTime.parse(data[5], timeFormatter);
    }

    public AttendanceRecord() {}

    // Method to load attendance records from an Excel file
    public static void loadAttendanceFromExcel(String filePath) {
        try {
            attendanceRecords = loadAttendance(filePath);
            System.out.println("Loaded " + attendanceRecords.size() + " attendance records.");
        } catch (IOException e) {
            System.err.println("Error loading attendance records: " + e.getMessage());
        }
    }

    // Method to read and parse attendance records from an Excel file
    public static ArrayList<AttendanceRecord> loadAttendance(String filePath) throws IOException {
        ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Iterate through rows, skipping header row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    String id = getCellValueAsString(row.getCell(0)).replace(".0", "");
                    String name = getCellValueAsString(row.getCell(1));
                    String surname = getCellValueAsString(row.getCell(2)).trim();

                    LocalDate date = parseDate(row.getCell(3));
                    LocalTime timeIn = parseTime(row.getCell(4));
                    LocalTime timeOut = parseTime(row.getCell(5));

                    // Skip records with missing time values
                    if (timeIn == null || timeOut == null) {
                        System.out.println("Skipping record with missing time values: " + id);
                        continue;
                    }

                    attendanceRecords.add(new AttendanceRecord(name + " " + surname, id, date, timeIn, timeOut));
                }
            }
        }

        return attendanceRecords;
    }

    // Method to parse a date value from an Excel cell
    private static LocalDate parseDate(Cell cell) {
        if (cell == null) {
            System.err.println("Date cell is null.");
            return null;
        }
        try {
            String cellValue = getCellValueAsString(cell);

            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                String dateString = cellValue.trim();
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

    // Method to parse a time value from an Excel cell
    private static LocalTime parseTime(Cell cell) {
        if (cell == null) {
            System.err.println("Time cell is null.");
            return null;
        }
        try {
            String cellValue = getCellValueAsString(cell);

            if (cell.getCellType() == CellType.NUMERIC) {
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

    // Method to get the value of an Excel cell as a string
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

    // Method to calculate the number of hours worked in a day
    public double calculateHoursWorked() {
        if (timeIn == null || timeOut == null) {
            System.err.println("TimeIn or TimeOut is null for record: " + id);
            return 0.0;
        }
        Duration duration;
        if (timeOut.isBefore(timeIn)) {
            duration = Duration.between(timeIn, timeOut.plusHours(24)); // Handles overnight shifts
        } else {
            duration = Duration.between(timeIn, timeOut);
        }
        return duration.toHours() + (duration.toMinutes() % 60) / 60.0;
    }

    // Method to calculate total hours worked in a specific week
    public static double calculateTotalHours(int year, int month, String employeeID, int week) {
        double totalHours = 0;

        for (AttendanceRecord record : attendanceRecords) {
            if (record.getId().equals(employeeID)) {
                LocalDate recordDate = record.getDate();
                if (recordDate.getYear() == year && recordDate.getMonthValue() == month) {
                    int recordWeek = getWeekOfMonth(recordDate);
                    if (recordWeek == week) {
                        double hoursWorked = record.calculateHoursWorked();
                        totalHours += hoursWorked;
                    }
                }
            }
        }

        return totalHours;
    }

    // Method to determine which week of the month a date belongs to
    public static int getWeekOfMonth(LocalDate date) {
        return ((date.getDayOfMonth() - 1) / 7) + 1;
    }

    // Getters for class attributes
    public String getName() { return name; }
    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
    public static ArrayList<AttendanceRecord> getAttendanceRecords() { return attendanceRecords; }
}
