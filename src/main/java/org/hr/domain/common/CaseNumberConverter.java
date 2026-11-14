package org.hr.domain.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Converter JPA pentru Value Object-ul {@link CaseNumber}: se stocheaza ca text (ER-2026-0001). */
@Converter
public class CaseNumberConverter implements AttributeConverter<CaseNumber, String> {
    @Override
    public String convertToDatabaseColumn(CaseNumber attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public CaseNumber convertToEntityAttribute(String dbData) {
        return dbData == null ? null : CaseNumber.parse(dbData);
    }
}