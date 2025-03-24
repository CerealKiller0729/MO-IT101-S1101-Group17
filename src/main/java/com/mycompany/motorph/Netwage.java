package com.mycompany.motorph;

import java.text.DecimalFormat;

public class Netwage extends Calculation {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private final Grosswage grosswage; // Grosswage object for the employee
    private final String employeeID; // Employee ID
    private final String employeeName; // Employee name
    private final double gross; // Gross wage
    private final double hours; // Total hours worked
    private final boolean isFirstHalf; // Flag to indicate if it's the first half of the month
    private final int targetMonth; // Target month for calculation

    // Constructor
    public Netwage(String employeeID, String employeeName, double gross, double hours, boolean isFirstHalf, Grosswage grosswage, int targetMonth) {
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.gross = gross;
        this.hours = hours;
        this.isFirstHalf = isFirstHalf;
        this.grosswage = grosswage; // Pass the Grosswage object
        this.targetMonth = targetMonth; // Set the target month
    }

    @Override
    public double calculate() {
        // Create instances of each deduction class using the actual Grosswage object
        WithholdingTax withholdingTax = new WithholdingTax(grosswage);
        Calculation sss = new SSS(grosswage);
        Calculation philhealth = new Philhealth(grosswage);
        Calculation pagibig = new Pagibig(grosswage);

        // Initialize LatePenalty with the correct employee ID, month, and hourly rate
        LatePenalty latePenalty = new LatePenalty(employeeID, targetMonth, grosswage.getHourlyRate());

        // Calculate each deduction
        double sssDeduction = sss.calculate();
        double philhealthDeduction = philhealth.calculate();
        double pagibigDeduction = pagibig.calculate();
        double lateDeduction = latePenalty.calculate();

        // Calculate total deductions
        double totalDeduction = sssDeduction + philhealthDeduction + pagibigDeduction + lateDeduction;

        // Calculate net wage
        double net = gross - totalDeduction;

        // Display results for the current half
        System.out.println("\n" + (isFirstHalf ? "First" : "Second") + " Half of the Month:");
        System.out.println("Total Hours Worked: " + decimalFormat.format(hours));
        System.out.println("Gross Wage: " + decimalFormat.format(gross));
        System.out.println("Net Wage: " + decimalFormat.format(net));

        return net; // Return the net wage
    }

    // Implement the getSSSDeduction method
    public double getSSSDeduction() {
        Calculation sss = new SSS(grosswage);
        return sss.calculate();
    }

    // Implement the getPhilhealthDeduction method
    public double getPhilhealthDeduction() {
        Calculation philhealth = new Philhealth(grosswage);
        return philhealth.calculate();
    }

    // Implement the getPagIbigDeduction method
    public double getPagIbigDeduction() {
        Calculation pagibig = new Pagibig(grosswage);
        return pagibig.calculate();
    }

    // Implement the getLateDeduction method
    public double getLateDeduction() {
        LatePenalty latePenalty = new LatePenalty(employeeID, targetMonth, grosswage.getHourlyRate());
        return latePenalty.calculate();
    }

    // Implement the getTotalDeductions method
    public double getTotalDeductions() {
        return getSSSDeduction() + getPhilhealthDeduction() + getPagIbigDeduction() + getLateDeduction();
    }

    // Implement the getTaxableIncome method
    public double getTaxableIncome() {
        return gross - getTotalDeductions();
    }

    // Implement the getWithholdingTax method
    public double getWithholdingTax() {
        WithholdingTax withholdingTax = new WithholdingTax(grosswage);
        return withholdingTax.calculate();
    }
}