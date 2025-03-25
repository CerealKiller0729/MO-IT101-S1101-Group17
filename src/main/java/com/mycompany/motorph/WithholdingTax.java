/*
 * Class for calculating withholding tax based on taxable income.
 */
package com.mycompany.motorph;

public class WithholdingTax extends Calculation {
    private double tax; // Withholding tax amount
    private double taxableIncome; // Taxable income
    private double afterTax; // Net income after tax
    private final Grosswage grosswage; // Gross wage object for calculation

    /**
     * Constructor for WithholdingTax.
     * @param grosswage The Grosswage object containing the employee's gross wage.
     */
    public WithholdingTax(Grosswage grosswage) {
        this.grosswage = grosswage;
    }

    /**
     * Calculates the withholding tax based on taxable income.
     * @return The net income after tax.
     */
    @Override
    public double calculate() {
        // Initialize other deductions
        Calculation sss = new SSS(grosswage);
        Calculation philhealth = new Philhealth(grosswage);
        Calculation pagibig = new Pagibig(grosswage);
        Calculation latePenalty = new LatePenalty();

        // Calculate total deductions
        double totalDeduction = sss.calculate() + philhealth.calculate() + pagibig.calculate() + latePenalty.calculate();

        // Compute taxable income
        taxableIncome = grosswage.calculate() - totalDeduction;

        // Calculate withholding tax based on taxable income brackets
        if (taxableIncome <= 20832) {
            tax = 0;
        } else if (taxableIncome <= 33333) {
            tax = (taxableIncome - 20832) * 0.20;
        } else if (taxableIncome <= 66667) {
            tax = 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome <= 166667) {
            tax = 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome <= 666667) {
            tax = 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            tax = 200833.33 + (taxableIncome - 666667) * 0.35;
        }

        // Return the withholding tax amount
        return tax; // Return the tax amount instead of afterTax
    }

    /**
     * Returns the withholding tax amount.
     * @return The withholding tax amount.
     */
    public double getTax() {
        return tax;
    }

    /**
     * Returns the taxable income.
     * @return The taxable income.
     */
    public double getTaxableIncome() {
        return taxableIncome;
    }

    /**
     * Returns the net income after tax.
     * @return The net income after tax.
     */
    public double getAfterTax() {
        return afterTax;
    }
}
