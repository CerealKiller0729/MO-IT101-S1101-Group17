package com.mycompany.motorph;

import java.time.LocalTime;
import java.util.List;
import java.time.LocalDate;

public class Grosswage extends Calculation {
    private final String employeeID;
    private final String employeeName;
    private double gross;
    private double hourlyRate;
    private double hoursWorked;
    private final int year;
    private final int month;
    private final int week;
    private final LocalTime shiftStartTime;
    private final boolean nightShift;

    // Detailed breakdown fields
    private double regularHours;
    private double overtimeHours;
    private double regularPay;
    private double overtimePay;
    private double holidayPay;

    // Constructor
    public Grosswage(String empId, String firstName, String lastName, int year, 
                     int month, int week, LocalTime shiftStartTime, boolean nightShift) {
        if (empId == null || empId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("Employee name cannot be null");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1-12");
        }
        if (week < 1 || week > 4) {
            throw new IllegalArgumentException("Week must be between 1-4");
        }
        if (year != 2024) {
            throw new IllegalArgumentException("This implementation only supports 2024 payroll");
        }
        if (shiftStartTime == null) {
            throw new IllegalArgumentException("Shift start time cannot be null");
        }

        // Initialize fields
        this.employeeID = empId;
        this.employeeName = firstName + " " + lastName;
        this.year = year;
        this.month = month;
        this.week = week;
        this.shiftStartTime = shiftStartTime;
        this.nightShift = nightShift;
    }

    // Override the calculate method to compute gross pay
    @Override
    public double calculate() {
        // Get the list of employees from the model
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
        Employee employee = findEmployeeById(employeeID, employees);

        // Check if employee exists
        if (employee == null) {
            throw new IllegalStateException("Employee ID " + employeeID + " not found");
        }

        // Set hourly rate from employee details
        hourlyRate = employee.getHourlyRate();
        if (hourlyRate <= 0) {
            throw new IllegalStateException("Invalid hourly rate for employee");
        }

        // Calculate the total hours worked in the target week
        hoursWorked = calculateWeeklyHoursWorked();
        if (hoursWorked < 0) {
            throw new IllegalStateException("Invalid hours worked calculation");
        }

        // Calculate pay including holiday rates
        calculatePayWithHolidayRates();
        gross = regularPay + overtimePay;
        
        // Validate the holiday pay to prevent overpaying
        validateHolidayPay();
        
        return gross;
    }

    // Method to calculate the total hours worked for the target week
    private double calculateWeeklyHoursWorked() {
        double totalHours = 0;
        List<AttendanceRecord> records = AttendanceRecord.getAttendanceRecords();

        for (AttendanceRecord record : records) {
            if (record.getId().equals(employeeID) && isDateInTargetWeek(record.getDate())) {
                totalHours += record.calculateHoursWorked();
            }
        }
        return totalHours;
    }

    // Check if the record date is in the target week
    private boolean isDateInTargetWeek(LocalDate date) {
        if (date.getYear() != year || date.getMonthValue() != month) {
            return false;
        }
        int weekOfMonth = ((date.getDayOfMonth() - 1) / 7) + 1;
        return weekOfMonth == week;
    }

    // Calculate the pay considering holiday rates
    private void calculatePayWithHolidayRates() {
        resetCounters();
        List<AttendanceRecord> records = AttendanceRecord.getAttendanceRecords();

        for (AttendanceRecord record : records) {
            if (record.getId().equals(employeeID) && isDateInTargetWeek(record.getDate())) {
                processDailyHours(record);
            }
        }
    }

    // Reset the pay and hour counters before starting the calculation
    private void resetCounters() {
        regularHours = 0;
        overtimeHours = 0;
        regularPay = 0;
        overtimePay = 0;
        holidayPay = 0;
    }

    // Process daily hours to apply pay rates
    private void processDailyHours(AttendanceRecord record) {
        LocalDate recordDate = record.getDate();
        double dailyHours = record.calculateHoursWorked();
        double dayRegular = Math.min(dailyHours, 8.0);
        double dayOvertime = Math.max(0, dailyHours - 8.0);

        // Apply holiday rates if the day is a holiday
        if (HolidayChecker.isHoliday(recordDate)) {
            applyHolidayRates(recordDate, dayRegular, dayOvertime);
        } else {
            applyRegularRates(dayRegular, dayOvertime);
        }
    }

    // Apply holiday rates for regular and overtime hours
    private void applyHolidayRates(LocalDate date, double regularHrs, double overtimeHrs) {
        double multiplier = HolidayChecker.getHolidayPayMultiplier(date);
        double holidayPremiumRate = multiplier - 1.0;
        
        regularPay += regularHrs * hourlyRate * multiplier;
        holidayPay += regularHrs * hourlyRate * holidayPremiumRate;
        
        if (overtimeHrs > 0) {
            double overtimeRate = nightShift ? 1.10 : 1.25;
            double baseOvertime = overtimeHrs * hourlyRate * overtimeRate;
            double overtimePremium = overtimeHrs * hourlyRate * holidayPremiumRate;
            
            overtimePay += baseOvertime;
            holidayPay += overtimePremium;
        }

        regularHours += regularHrs;
        overtimeHours += overtimeHrs;
    }

    // Apply regular pay rates for normal working days
    private void applyRegularRates(double regularHrs, double overtimeHrs) {
        regularPay += regularHrs * hourlyRate;
        
        if (overtimeHrs > 0) {
            double overtimeMultiplier = nightShift ? 1.10 : 1.25;
            overtimePay += overtimeHrs * hourlyRate * overtimeMultiplier;
        }
        
        regularHours += regularHrs;
        overtimeHours += overtimeHrs;
    }

    // Validate holiday pay to ensure it does not exceed the maximum allowable premium
    private void validateHolidayPay() {
        double maxExpectedPremium = (regularHours + overtimeHours) * hourlyRate * 1.3;
        if (holidayPay > maxExpectedPremium) {
            throw new IllegalStateException(
                String.format("Holiday pay %.2f exceeds reasonable maximum (%.2f)", 
                holidayPay, maxExpectedPremium));
        }
    }

    // Getters for the calculated fields
    public double getRegularHours() { return regularHours; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getRegularPay() { return regularPay; }
    public double getOvertimePay() { return overtimePay; }
    public double getHolidayPay() { return holidayPay; }
    public String getEmployeeID() { return employeeID; }
    public String getEmployeeName() { return employeeName; }
    public double getHourlyRate() { return hourlyRate; }
    public double getHoursWorked() { return hoursWorked; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getWeek() { return week; }
    public LocalTime getShiftStartTime() { return shiftStartTime; }
    public boolean isNightShift() { return nightShift; }

    // Find employee by ID from the list of employees
    private Employee findEmployeeById(String employeeId, List<Employee> employees) {
        return employees.stream()
            .filter(e -> e != null && e.getEmployeeNumber().equals(employeeId))
            .findFirst()
            .orElse(null);
    }

    // Print detailed calculation of wages
    public void printCalculationDetails() {
        System.out.println("\nCalculation Details:");
        System.out.printf("Hourly Rate: PHP %.2f%n", hourlyRate);
        System.out.printf("Regular Hours: %.2f (PHP %.2f)%n", regularHours, regularPay);
        System.out.printf("Overtime Hours: %.2f (PHP %.2f)%n", overtimeHours, overtimePay);
        System.out.printf("Holiday Premium Pay: PHP %.2f%n", holidayPay);
        System.out.printf("Total Gross: PHP %.2f%n", gross);
    }
}
