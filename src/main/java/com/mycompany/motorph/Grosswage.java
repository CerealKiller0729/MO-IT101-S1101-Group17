/*
 * Class for calculating the gross wage of an employee based on hours worked and hourly rate.
 */
package com.mycompany.motorph;

import java.util.List;

public class Grosswage extends Calculation {
    private final String employeeID; // Employee ID
    private final String employeeName; // Employee name
    private double gross; // Gross wage
    private double hourlyRate; // Hourly rate
    private double hoursWorked; // Total hours worked
    private final int year; // Year for calculation
    private final int month; // Month for calculation
    private final boolean isFirstHalf; // Flag to indicate if it's the first half of the month

    /**
     * Constructor for Grosswage.
     *
     * @param empId       The ID of the employee.
     * @param firstName   The first name of the employee.
     * @param lastName    The last name of the employee.
     * @param year        The year for calculation.
     * @param month       The month for calculation.
     * @param isFirstHalf Flag to indicate if it's the first half of the month.
     */
    public Grosswage(String empId, String firstName, String lastName, int year, int month, boolean isFirstHalf) {
        this.employeeID = empId;
        this.employeeName = firstName + " " + lastName;
        this.year = year;
        this.month = month;
        this.isFirstHalf = isFirstHalf;
    }

    @Override
    public double calculate() {
        // Get the list of employees
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();

        // Find the employee by ID
        Employee employee = findEmployeeById(employeeID, employees);
        if (employee == null) {
            System.err.println("Employee ID " + employeeID + " not found.");
            return 0; // Exit if the employee is not found
        }

        // Retrieve the hourly rate from the employee object
        hourlyRate = employee.getHourlyRate();
        if (hourlyRate <= 0) {
            System.err.println("Invalid hourly rate for Employee ID " + employeeID + ": " + hourlyRate);
            return 0; // Exit if the hourly rate is invalid
        }

        // Calculate total hours worked for the first or second half of the month
        hoursWorked = AttendanceRecord.calculateTotalHours(year, month, employeeID, isFirstHalf);
        if (hoursWorked < 0) {
            System.err.println("Invalid hours worked for Employee ID " + employeeID + ": " + hoursWorked);
            return 0; // Exit if the hours worked are invalid
        }

        // Calculate gross wage
        gross = calculateGrossWage(hoursWorked);

        return gross; // Return the gross wage
    }

    /**
     * Finds an employee by their ID in the list of employees.
     *
     * @param employeeId The ID of the employee to find.
     * @param employees  The list of employees to search.
     * @return The found employee, or null if not found.
     */
    private Employee findEmployeeById(String employeeId, List<Employee> employees) {
        for (Employee employee : employees) {
            if (employee.getEmployeeNumber().equals(employeeId)) {
                return employee; // Return the found employee
            }
        }
        return null; // Return null if not found
    }

    /**
     * Calculates the gross wage based on hours worked and hourly rate.
     *
     * @param totalHours The total hours worked by the employee.
     * @return The calculated gross wage.
     */
    private double calculateGrossWage(double totalHours) {
        return totalHours * hourlyRate; // Gross wage = hours worked * hourly rate
    }

    // Getters
    public String getEmployeeID() {
        return employeeID;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public double getGross() {
        return gross;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }
}