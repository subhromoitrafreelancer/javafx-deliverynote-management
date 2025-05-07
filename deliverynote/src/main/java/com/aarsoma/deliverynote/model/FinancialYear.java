package com.aarsoma.deliverynote.model;

import java.time.LocalDate;
import java.time.Month;

public class FinancialYear {

    public static String getCurrentFinancialYear() {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        Month currentMonth = today.getMonth();

        if (currentMonth.getValue() >= Month.APRIL.getValue()) {
            return currentYear + "-" + (currentYear + 1);
        } else {
            return (currentYear - 1) + "-" + currentYear;
        }
    }

    public static LocalDate getFinancialYearStartDate() {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        Month currentMonth = today.getMonth();

        if (currentMonth.getValue() >= Month.APRIL.getValue()) {
            return LocalDate.of(currentYear, Month.APRIL, 1);
        } else {
            return LocalDate.of(currentYear - 1, Month.APRIL, 1);
        }
    }

    public static LocalDate getFinancialYearEndDate() {
        return getFinancialYearStartDate().plusYears(1).minusDays(1);
    }
}
