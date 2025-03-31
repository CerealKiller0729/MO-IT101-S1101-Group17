package com.mycompany.motorph;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Netwage extends Calculation {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");  // Format for displaying wage amounts
    private final Grosswage grosswage;  // Grosswage object, which is used to retrieve gross wage and hourly rate
    private final String employeeID;  // Employee's unique identifier
    private final String employeeName;  // Employee's full name
    private final double gross;  // Employee's gross wage for the period
    private final double hours;  // Total working hours for the employee
    private final int week;  // Target week number (1-4)
    private final int targetMonth;  // Target month for wage calculation (1-12)
    private final int targetYear;  // Target year for wage calculation
    
    // Cached calculations to avoid recalculating repeatedly
    private Double sssDeduction;  // Social Security System deduction
    private Double philhealthDeduction;  // PhilHealth deduction
    private Double pagibigDeduction;  // Pag-IBIG Fund deduction
    private Double lateDeduction;  // Deduction for being late
    private Double withholdingTax;  // Tax deduction based on taxable income

    // Constructor for initializing the Netwage object with essential parameters
    public Netwage(String employeeID, String employeeName, double gross, double hours, 
                  int week, Grosswage grosswage, int targetMonth, int targetYear) {
        // Validate inputs to ensure they are not null/empty and within reasonable ranges
        if (employeeID == null || employeeID.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }
        if (employeeName == null || employeeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be null or empty");
        }
        if (gross < 0) {
            throw new IllegalArgumentException("Gross wage cannot be negative");
        }
        if (hours < 0) {
            throw new IllegalArgumentException("Hours cannot be negative");
        }
        if (grosswage == null) {
            throw new IllegalArgumentException("Grosswage cannot be null");
        }
        if (targetMonth < 1 || targetMonth > 12) {
            throw new IllegalArgumentException("Month must be between 1-12");
        }
        if (week < 1 || week > 4) {
            throw new IllegalArgumentException("Week must be between 1-4");
        }
        if (targetYear < 2000 || targetYear > LocalDate.now().getYear() + 1) {
            throw new IllegalArgumentException("Invalid year");
        }

        // Assigning values to instance variables after validation
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.gross = gross;
        this.hours = hours;
        this.week = week;
        this.grosswage = grosswage;
        this.targetMonth = targetMonth;
        this.targetYear = targetYear;
    }

    // Method to calculate net wage by deducting various deductions and withholding tax from the gross wage
    @Override
    public double calculate() {
        double totalDeductions = getTotalDeductions();  // Get total deductions (SSS, PhilHealth, Pag-Ibig, Late penalty)
        double withholdingTax = getWithholdingTax();  // Get the withholding tax
        double netWage = gross - totalDeductions - withholdingTax;  // Calculate net wage
        return Double.parseDouble(decimalFormat.format(netWage));  // Format and return the result
    }

    // Calculate the SSS deduction based on the employee's gross wage
    public double getSSSDeduction() {
        if (sssDeduction == null) {
            Calculation sss = new SSS(grosswage);  // Create an SSS deduction calculation object
            sssDeduction = sss.calculate() / 4;  // Divide by 4 to get weekly deduction (monthly deduction / 4)
        }
        return sssDeduction;  // Return the cached SSS deduction value
    }

    // Calculate the PhilHealth deduction based on the employee's gross wage
    public double getPhilhealthDeduction() {
        if (philhealthDeduction == null) {
            Calculation philhealth = new Philhealth(grosswage);  // Create a PhilHealth deduction calculation object
            philhealthDeduction = philhealth.calculate() / 4;  // Divide by 4 to get weekly deduction
        }
        return philhealthDeduction;  // Return the cached PhilHealth deduction value
    }

    // Calculate the Pag-IBIG Fund deduction based on the employee's gross wage
    public double getPagIbigDeduction() {
        if (pagibigDeduction == null) {
            Calculation pagibig = new Pagibig(grosswage);  // Create a Pag-Ibig deduction calculation object
            pagibigDeduction = pagibig.calculate() / 4;  // Divide by 4 to get weekly deduction
        }
        return pagibigDeduction;  // Return the cached Pag-IBIG deduction value
    }

    // Calculate any deductions due to late attendance
    public double getLateDeduction() {
        if (lateDeduction == null) {
            lateDeduction = calculateWeeklyLatePenalty();  // Calculate penalty based on late arrivals
        }
        return lateDeduction;  // Return the cached late penalty deduction value
    }

    // Method to calculate the weekly late penalty for attendance
    private double calculateWeeklyLatePenalty() {
        List<AttendanceRecord> records = AttendanceRecord.getAttendanceRecords();  // Get the attendance records
        double totalPenalty = 0.0;  // Variable to accumulate total penalty
        final double minuteRate = grosswage.getHourlyRate() / 60.0;  // Hourly rate per minute
        final LocalTime shiftStart = grosswage.getShiftStartTime();  // Shift start time for the employee
        final LocalTime lateThreshold = shiftStart.plusMinutes(15);  // Late threshold time (15 minutes grace period)

        // Iterate through the attendance records and calculate penalties for late arrivals
        for (AttendanceRecord record : records) {
            if (record.getId().equals(employeeID) && isDateInTargetWeek(record.getDate())) {
                LocalTime timeIn = record.getTimeIn();  // Time the employee clocked in
                if (timeIn != null && timeIn.isAfter(lateThreshold)) {
                    long minutesLate = java.time.Duration.between(lateThreshold, timeIn).toMinutes();  // Calculate minutes late
                    totalPenalty += minuteRate * minutesLate;  // Add penalty for the late minutes
                }
            }
        }
        return totalPenalty;  // Return the total late penalty for the week
    }

    // Check if a given date is within the target week
    private boolean isDateInTargetWeek(LocalDate date) {
        if (date.getYear() != targetYear || date.getMonthValue() != targetMonth) {
            return false;  // Date is not in the target year or month
        }
        int weekOfMonth = ((date.getDayOfMonth() - 1) / 7) + 1;  // Calculate which week of the month the date falls in
        return weekOfMonth == week;  // Return true if the date is within the target week
    }

    // Get the total deductions including SSS, PhilHealth, Pag-IBIG, and late penalty
    public double getTotalDeductions() {
        return getSSSDeduction() + 
               getPhilhealthDeduction() + 
               getPagIbigDeduction() + 
               getLateDeduction();  // Sum all deductions
    }

    // Get the taxable income after deductions
    public double getTaxableIncome() {
        return gross - getTotalDeductions();  // Gross wage minus total deductions
    }

    // Calculate the withholding tax based on taxable income
    public double getWithholdingTax() {
        if (withholdingTax == null) {
            double taxableIncome = getTaxableIncome();  // Calculate the taxable income
            WithholdingTax withholdingTaxCalc = new WithholdingTax(grosswage, taxableIncome);  // Create a withholding tax calculator object
            withholdingTax = withholdingTaxCalc.calculate();  // Calculate withholding tax
        }
        return withholdingTax;  // Return the cached withholding tax value
    }

    // Getters for retrieving various employee data and calculated values
    public Grosswage getGrosswage() { return grosswage; }
    public String getEmployeeID() { return employeeID; }
    public String getEmployeeName() { return employeeName; }
    public double getGross() { return gross; }
    public double getHours() { return hours; }
    public int getWeek() { return week; }
    public int getTargetMonth() { return targetMonth; }
    public int getTargetYear() { return targetYear; }
}
