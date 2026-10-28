package com.cargaglobal.mobilebackend.infrastructure.persistence;

import com.cargaglobal.mobilebackend.domain.model.FaceComparison;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.ObjectMapper;

@Converter
public class FaceComparisonConverter implements AttributeConverter<FaceComparison,String> {
    private static final ObjectMapper JSON = new ObjectMapper();
    public String convertToDatabaseColumn(FaceComparison value) {
        return value == null ? null : JSON.writeValueAsString(value);
    }
    public FaceComparison convertToEntityAttribute(String value) {
        return value == null ? null : JSON.readValue(value,FaceComparison.class);
    }
}
