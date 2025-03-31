package com.mycompany.motorph;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.time.DateTimeException;

/**
 * This class calculates the late penalty for employees based on their attendance
 * for a specific month, year, and week. The penalty is calculated based on the 
 * employee's lateness beyond a grace period after their scheduled shift start time.
 */
public class LatePenalty extends Calculation {
    private final String targetEmployeeID;  // Employee ID for which the penalty is calculated
    private final int targetMonth;          // Target month for the calculation
    private final int targetYear;           // Target year for the calculation
    private final int week;                 // The week (1-4) within the month
    private final double hourlyRate;        // Employee's hourly rate for penalty calculation
    private final LocalTime shiftStartTime; // Scheduled shift start time for the employee
    
    // Constants
    private static final int GRACE_PERIOD_MINUTES = 15;  // Grace period in minutes
    private static final LocalTime SHIFT_8AM = LocalTime.of(8, 0);  // 8:00 AM shift
    private static final LocalTime SHIFT_9AM = LocalTime.of(9, 0);  // 9:00 AM shift
    private static final LocalTime SHIFT_10AM = LocalTime.of(10, 0);  // 10:00 AM shift

    // Enum to represent different payroll cycles (weekly, first half, second half)
    public enum PayrollCycle {
        WEEKLY, FIRST_HALF, SECOND_HALF
    }

    /**
     * Constructor for the LatePenalty class that initializes the instance variables.
     * Validates input values to ensure they are within the allowed range.
     */
    public LatePenalty(String targetEmployeeID, int targetMonth, int targetYear, 
                       int week, double hourlyRate, LocalTime shiftStartTime) {
        if (targetEmployeeID == null || targetEmployeeID.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }
        if (targetMonth < 1 || targetMonth > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (targetYear < 2000 || targetYear > LocalDate.now().getYear() + 1) {
            throw new IllegalArgumentException("Invalid year");
        }
        if (week < 1 || week > 4) {
            throw new IllegalArgumentException("Week must be between 1-4");
        }
        if (hourlyRate <= 0) {
            throw new IllegalArgumentException("Hourly rate must be positive");
        }
        if (shiftStartTime == null) {
            throw new IllegalArgumentException("Shift start time cannot be null");
        }
        // Validate shift start time against allowed values
        if (!shiftStartTime.equals(SHIFT_8AM) && 
            !shiftStartTime.equals(SHIFT_9AM) && 
            !shiftStartTime.equals(SHIFT_10AM)) {
            throw new IllegalArgumentException("Shift must be exactly 8:00, 9:00, or 10:00 AM");
        }

        // Initialize instance variables
        this.targetEmployeeID = targetEmployeeID;
        this.targetMonth = targetMonth;
        this.targetYear = targetYear;
        this.week = week;
        this.hourlyRate = hourlyRate;
        this.shiftStartTime = shiftStartTime;
    }

    /**
     * Calculates the total late penalty for the employee based on attendance records.
     * Iterates through the attendance records, checks if the employee was late,
     * and calculates the penalty based on the difference in time after the grace period.
     * 
     * @return the total late deduction for the target employee
     */
    @Override
    public double calculate() {
        try {
            // Get the first and last dates for the week in the target month
            YearMonth yearMonth = YearMonth.of(targetYear, targetMonth);
            LocalDate startDate = calculateWeekStartDate(yearMonth);
            LocalDate endDate = calculateWeekEndDate(yearMonth);
            
            double totalLateDeduction = 0;
            
            // Get all attendance records
            List<AttendanceRecord> attendanceRecords = AttendanceRecord.getAttendanceRecords();

            for (AttendanceRecord record : attendanceRecords) {
                if (record != null && record.getId().equals(targetEmployeeID)) {
                    LocalDate recordDate = record.getDate();
                    if (recordDate != null && 
                        !recordDate.isBefore(startDate) && 
                        !recordDate.isAfter(endDate)) {
                        
                        LocalTime timeIn = record.getTimeIn();
                        if (timeIn != null) {
                            // Calculate late penalty if the employee arrived after the grace period
                            LocalTime lateThreshold = shiftStartTime.plusMinutes(GRACE_PERIOD_MINUTES);
                            if (timeIn.isAfter(lateThreshold)) {
                                long minutesLate = java.time.Duration.between(lateThreshold, timeIn).toMinutes();
                                double deduction = (hourlyRate / 60.0) * minutesLate;  // Penalty based on hourly rate
                                totalLateDeduction += Math.max(0, deduction);  // Avoid negative deductions
                            }
                        }
                    }
                }
            }
            return totalLateDeduction;  // Return the total late penalty
        } catch (DateTimeException e) {
            throw new IllegalStateException("Failed to calculate late penalty: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates the start date of the target week based on the given month and year.
     * 
     * @param yearMonth the YearMonth representing the target month and year
     * @return the LocalDate representing the start date of the week
     */
    private LocalDate calculateWeekStartDate(YearMonth yearMonth) {
        int startDay = 1 + (week - 1) * 7;  // First day of the target week
        return yearMonth.atDay(Math.min(startDay, yearMonth.lengthOfMonth()));  // Return the valid start date
    }

    /**
     * Calculates the end date of the target week based on the given month and year.
     * 
     * @param yearMonth the YearMonth representing the target month and year
     * @return the LocalDate representing the end date of the week
     */
    private LocalDate calculateWeekEndDate(YearMonth yearMonth) {
        int endDay = week * 7;  // Last day of the target week
        return yearMonth.atDay(Math.min(endDay, yearMonth.lengthOfMonth()));  // Return the valid end date
    }

    // Getters for the instance variables
    public String getTargetEmployeeID() { return targetEmployeeID; }
    public int getTargetMonth() { return targetMonth; }
    public int getTargetYear() { return targetYear; }
    public int getWeek() { return week; }
    public double getHourlyRate() { return hourlyRate; }
    public LocalTime getShiftStartTime() { return shiftStartTime; }
}
