package org.hr.domain.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Value;

import java.io.Serializable;

/** Value Object (custom data type), CR-04: ER-&lt;an&gt;-&lt;numar secvential pe 4 cifre&gt;, ex. ER-2026-0001. */
@Value
public class CaseNumber implements Serializable, Comparable<CaseNumber> {
    @Min(value = 2000, message = "Case year is invalid!")
    int year;

    @Min(value = 1, message = "Case sequence must be at least 1!")
    @Max(value = 9999, message = "Case sequence cannot exceed 9999!")
    int sequence;

    public static CaseNumber of(int year, int sequence) {
        return new CaseNumber(year, sequence);
    }

    public static CaseNumber parse(String text) {
        if (text == null || !text.matches("ER-\\d{4}-\\d{4}"))
            throw new IllegalArgumentException("Invalid case number: " + text);
        return new CaseNumber(Integer.parseInt(text.substring(3, 7)), Integer.parseInt(text.substring(8)));
    }

    @Override
    public String toString() {
        return String.format("ER-%d-%04d", year, sequence);
    }

    @Override
    public int compareTo(CaseNumber other) {
        int byYear = Integer.compare(year, other.year);
        return byYear != 0 ? byYear : Integer.compare(sequence, other.sequence);
    }
}
