package com.example.ai_interview.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** InterviewStatus 枚举与数据库小写字符串的转换器 */
@Converter(autoApply = true)
public class InterviewStatusConverter implements AttributeConverter<InterviewStatus, String> {

    @Override
    public String convertToDatabaseColumn(InterviewStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public InterviewStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : InterviewStatus.from(dbData);
    }
}
