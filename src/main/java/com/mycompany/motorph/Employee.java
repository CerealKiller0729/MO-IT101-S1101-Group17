/*
 * Class representing an Employee with personal and employment details.
 */
package com.mycompany.motorph;

import java.text.DecimalFormat;
/**
 *
 * @author angeliquerivera
 */

public class Employee {

    // Employee details
    private String employeeNumber;
    private String lastName;
    private String firstName;
    private String birthday;
    private String address;
    private String phoneNumber;
    private String sssNumber;
    private String philhealthNumber;
    private String tinNumber;
    private String pagIbigNumber;
    private String status;
    private String position;
    private String immediateSupervisor;
    private String basicSalary;
    private String riceSubsidy;
    private String phoneAllowance;
    private String clothingAllowance;
    private String grossSemiMonthlyRate;
    private double hourlyRate;

    // Decimal formatter for consistent number formatting
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    /**
     * Constructor to initialize an Employee object with data from a String array.
     * @param data An array of strings containing employee data.
     */
    public Employee(String[] data) {
        if (data.length < 19) {
            throw new IllegalArgumentException("Insufficient data to create Employee object.");
        }

        this.employeeNumber = parseEmployeeNumber(data[0]);
        this.lastName = getValue(data, 1);
        this.firstName = getValue(data, 2);
        this.birthday = getValue(data, 3);
        this.address = getValue(data, 4);
        this.phoneNumber = getValue(data, 5);
        this.sssNumber = getValue(data, 6);
        this.philhealthNumber = getValue(data, 7);
        this.tinNumber = getValue(data, 8);
        this.pagIbigNumber = getValue(data, 9);
        this.status = getValue(data, 10);
        this.position = getValue(data, 11);
        this.immediateSupervisor = getValue(data, 12);
        this.basicSalary = getValue(data, 13);
        this.riceSubsidy = getValue(data, 14);
        this.phoneAllowance = getValue(data, 15);
        this.clothingAllowance = getValue(data, 16);
        this.grossSemiMonthlyRate = getValue(data, 17);
        this.hourlyRate = parseDoubleValue(data[18]);
    }

    /**
     * Parses the employee number from a string, ensuring it is in the correct format.
     * @param employeeNumber The raw employee number string.
     * @return The parsed employee number as a string.
     */
    private String parseEmployeeNumber(String employeeNumber) {
        try {
            // Attempt to parse the employee number as a double and convert it to an integer
            double empNum = Double.parseDouble(employeeNumber);
            return String.valueOf((int) empNum); // Remove decimal points
        } catch (NumberFormatException e) {
            System.err.println("Error parsing employee number: " + employeeNumber);
            return employeeNumber; // Return the original value if parsing fails
        }
    }

    /**
     * Parses a string value into a double.
     * @param value The string value to parse.
     * @return The parsed double value, or 0.0 if parsing fails.
     */
    private double parseDoubleValue(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            // Remove non-numeric characters (e.g., currency symbols, commas)
            value = value.replaceAll("[^0-9.]", "");
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing double value: " + value);
            return 0.0; // Return 0.0 if parsing fails
        }
    }

    /**
     * Gets a value from the data array at the specified index.
     * @param data The data array.
     * @param index The index of the value to retrieve.
     * @return The value at the specified index, or an empty string if the index is out of bounds.
     */
    private String getValue(String[] data, int index) {
        return (data.length > index) ? data[index] : "";
    }

    /**
     * Returns a formatted string representation of the employee's details.
     * @return A string containing the employee's details.
     */
    @Override
    public String toString() {
        return String.format("""
                Employee ID: %s
                Name: %s %s
                Birthday: %s
                Address: %s
                Phone Number: %s
                SSS Number: %s
                PhilHealth Number: %s
                TIN: %s
                PAG-IBIG Number: %s
                Status: %s
                Position: %s
                Immediate Supervisor: %s
                Basic Salary: %s
                Rice Subsidy: %s
                Phone Allowance: %s
                Clothing Allowance: %s
                Gross Semi-Monthly Rate: %s
                Hourly Rate: %.2f
                """,
                employeeNumber,
                lastName,
                firstName,
                birthday,
                address,
                phoneNumber,
                sssNumber,
                philhealthNumber,
                tinNumber,
                pagIbigNumber,
                status,
                position,
                immediateSupervisor,
                basicSalary,
                riceSubsidy,
                phoneAllowance,
                clothingAllowance,
                grossSemiMonthlyRate,
                hourlyRate
        );
    }

    /**
     * Returns a formatted string representation of the employee's details for display.
     * @param detailed If true, includes all details; otherwise, includes only basic details.
     * @return A string containing the employee's details.
     */
    public String toString(boolean detailed) {
        if (detailed) {
            return toString();
        } else {
            return String.format("""
                    Employee ID: %s
                    Name: %s %s
                    Position: %s
                    Hourly Rate: %.2f
                    """,
                    employeeNumber,
                    lastName,
                    firstName,
                    position,
                    hourlyRate
            );
        }
    }

    // Getters and Setters

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getSssNumber() {
        return sssNumber;
    }

    public String getPhilhealthNumber() {
        return philhealthNumber;
    }

    public String getTinNumber() {
        return tinNumber;
    }

    public String getPagIbigNumber() {
        return pagIbigNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getPosition() {
        return position;
    }

    public String getImmediateSupervisor() {
        return immediateSupervisor;
    }

    public String getBasicSalary() {
        return basicSalary;
    }

    public String getRiceSubsidy() {
        return riceSubsidy;
    }

    public String getPhoneAllowance() {
        return phoneAllowance;
    }

    public String getClothingAllowance() {
        return clothingAllowance;
    }

    public String getGrossSemiMonthlyRate() {
        return grossSemiMonthlyRate;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}