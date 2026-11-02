package org.hr.services;

import java.time.LocalDate;

/** Calendar al zilelor nelucratoare (sarbatori legale). */
public interface IHolidayCalendar {
    boolean isHoliday(LocalDate date);
}
