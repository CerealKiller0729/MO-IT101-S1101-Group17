package com.mycompany.motorph;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;
import java.util.HashSet;

/**
 * This class contains Philippine holidays for the year 2024 only.
 * - Regular holidays provide 200% pay.
 * - Special non-working days provide 130% pay.
 */
public class HolidayChecker {

    // Set to store regular holidays (200% pay)
    private static final Set<LocalDate> REGULAR_HOLIDAYS = new HashSet<>();
    
    // Set to store special non-working days (130% pay)
    private static final Set<LocalDate> SPECIAL_NON_WORKING_DAYS = new HashSet<>();

    // Static block to initialize holiday sets
    static {
        // Adding regular holidays to the set (200% pay)
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.JANUARY, 1));   // New Year's Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.APRIL, 9));     // Araw ng Kagitingan
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.APRIL, 10));    // Eid'l Fitr
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.MAY, 1));       // Labor Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.JUNE, 12));     // Independence Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.JUNE, 17));     // Eid'l Adha
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.AUGUST, 26));   // National Heroes Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.NOVEMBER, 30)); // Bonifacio Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.DECEMBER, 25)); // Christmas Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.DECEMBER, 30)); // Rizal Day

        // Adding special non-working days to the set (130% pay)
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.FEBRUARY, 10)); // Chinese New Year
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.MARCH, 28));    // Maundy Thursday
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.MARCH, 29));    // Good Friday
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.MARCH, 30));    // Black Saturday
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.AUGUST, 21));   // Ninoy Aquino Day
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.NOVEMBER, 1));  // All Saints' Day
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.DECEMBER, 8));  // Immaculate Conception
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.DECEMBER, 31)); // New Year's Eve
    }

    /**
     * Checks if a given date is a regular holiday (200% pay).
     * 
     * @param date the date to check
     * @return true if the date is a regular holiday, false otherwise
     */
    public static boolean isRegularHoliday(LocalDate date) {
        return REGULAR_HOLIDAYS.contains(date);
    }

    /**
     * Checks if a given date is a special non-working day (130% pay).
     * 
     * @param date the date to check
     * @return true if the date is a special non-working day, false otherwise
     */
    public static boolean isSpecialNonWorkingDay(LocalDate date) {
        return SPECIAL_NON_WORKING_DAYS.contains(date);
    }

    /**
     * Checks if a given date is either a regular holiday or a special non-working day.
     * 
     * @param date the date to check
     * @return true if the date is either a regular holiday or a special non-working day
     */
    public static boolean isHoliday(LocalDate date) {
        return isRegularHoliday(date) || isSpecialNonWorkingDay(date);
    }

    /**
     * Gets the pay multiplier for the given date.
     * - 2.0 for regular holidays (200% pay)
     * - 1.3 for special non-working days (130% pay)
     * - 1.0 otherwise (normal pay)
     * 
     * @param date the date to check
     * @return the pay multiplier for the given date
     */
    public static double getHolidayPayMultiplier(LocalDate date) {
        if (isRegularHoliday(date)) return 2.0;  // 200% pay for regular holidays
        if (isSpecialNonWorkingDay(date)) return 1.3; // 130% pay for special non-working days
        return 1.0;  // Normal pay for other days
    }
}
