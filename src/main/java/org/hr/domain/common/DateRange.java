package org.hr.domain.common;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

/** Value Object (custom data type): interval calendaristic inclusiv. */
@Value
public class DateRange implements Serializable {
    @NotNull(message = "Start date is required!")
    LocalDate startDate;

    @NotNull(message = "End date is required!")
    LocalDate endDate;

    public static DateRange of(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }

    @AssertTrue(message = "End date must not be before start date!")
    public boolean isChronological() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    public boolean overlaps(DateRange other) {
        return !this.endDate.isBefore(other.startDate) && !other.endDate.isBefore(this.startDate);
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
