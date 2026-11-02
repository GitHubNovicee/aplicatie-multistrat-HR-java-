package org.hr.services;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

/**
 * Sarbatori legale din Romania (zile libere): sarbatori cu data fixa + sarbatori legate de Pastele ortodox.
 * Pastele ortodox se calculeaza cu algoritmul Meeus (calendar iulian) + 13 zile (valabil 1900-2099).
 */
public class RomanianLegalHolidayCalendar implements IHolidayCalendar {

    @Override
    public boolean isHoliday(LocalDate date) {
        return holidaysOf(date.getYear()).contains(date);
    }

    public Set<LocalDate> holidaysOf(int year) {
        Set<LocalDate> holidays = new HashSet<>();
        // cu data fixa
        holidays.add(LocalDate.of(year, Month.JANUARY, 1));
        holidays.add(LocalDate.of(year, Month.JANUARY, 2));
        holidays.add(LocalDate.of(year, Month.JANUARY, 6));   // Boboteaza
        holidays.add(LocalDate.of(year, Month.JANUARY, 7));   // Sf. Ion
        holidays.add(LocalDate.of(year, Month.JANUARY, 24));  // Ziua Unirii Principatelor
        holidays.add(LocalDate.of(year, Month.MAY, 1));
        holidays.add(LocalDate.of(year, Month.JUNE, 1));      // Ziua Copilului
        holidays.add(LocalDate.of(year, Month.AUGUST, 15));   // Adormirea Maicii Domnului
        holidays.add(LocalDate.of(year, Month.NOVEMBER, 30)); // Sf. Andrei
        holidays.add(LocalDate.of(year, Month.DECEMBER, 1));  // Ziua Nationala
        holidays.add(LocalDate.of(year, Month.DECEMBER, 25));
        holidays.add(LocalDate.of(year, Month.DECEMBER, 26));
        // legate de Pastele ortodox
        LocalDate easter = orthodoxEaster(year);
        holidays.add(easter.minusDays(2)); // Vinerea Mare
        holidays.add(easter);              // Duminica Pastelui
        holidays.add(easter.plusDays(1));  // Lunea Pastelui
        holidays.add(easter.plusDays(49)); // Rusalii (duminica)
        holidays.add(easter.plusDays(50)); // Lunea Rusaliilor
        return holidays;
    }

    /** Data Pastelui ortodox in calendarul gregorian. */
    public LocalDate orthodoxEaster(int year) {
        int a = year % 4;
        int b = year % 7;
        int c = year % 19;
        int d = (19 * c + 15) % 30;
        int e = (2 * a + 4 * b - d + 34) % 7;
        int month = (d + e + 114) / 31;
        int day = ((d + e + 114) % 31) + 1;
        return LocalDate.of(year, month, day).plusDays(13);
    }
}
