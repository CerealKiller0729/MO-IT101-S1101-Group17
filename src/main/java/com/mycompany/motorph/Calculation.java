/*
 * Abstract class for performing calculations.
 * This class serves as a base for specific calculation implementations.
 */
package com.mycompany.motorph;

import java.text.DecimalFormat;

/**
 * The Calculation class provides a base structure for performing various calculations.
 * It includes a decimal formatter for consistent number formatting.
 * This class is abstract and must be extended by subclasses implementing specific calculations.
 * 
 * @author angeliquerivera
 */
public abstract class Calculation {

    // Decimal formatter for consistent number formatting (e.g., rounding to 2 decimal places)
    protected static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    /**
     * Abstract method to perform the calculation.
     * Subclasses must provide an implementation for this method.
     * 
     * @return The result of the calculation as a double.
     */
    protected abstract double calculate();

    /**
     * Formats a numeric value using the decimal formatter.
     * Ensures the output is rounded to two decimal places for consistency.
     * 
     * @param value The value to format.
     * @return The formatted value as a String.
     */
    protected String format(double value) {
        return decimalFormat.format(value);
    }
}
