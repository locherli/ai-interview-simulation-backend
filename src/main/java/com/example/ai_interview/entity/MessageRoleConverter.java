package com.example.ai_interview.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** MessageRole 枚举与数据库小写字符串的转换器 */
@Converter(autoApply = true)
public class MessageRoleConverter implements AttributeConverter<MessageRole, String> {

    @Override
    public String convertToDatabaseColumn(MessageRole attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public MessageRole convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MessageRole.from(dbData);
    }
}
