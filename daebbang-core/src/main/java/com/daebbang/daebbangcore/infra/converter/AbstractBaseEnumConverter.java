package com.daebbang.daebbangcore.infra.converter;

import com.daebbang.daebbangcommon.converter.BaseEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Objects;
import java.util.stream.Stream;

@Converter
public class AbstractBaseEnumConverter<E extends BaseEnum<T>, T> implements
    AttributeConverter<E, T> {

    private final Class<E> enumClass;

    protected AbstractBaseEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public T convertToDatabaseColumn(E attribute) {
        if (Objects.isNull(attribute)) return null;

        return attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(T dbData) {
        if (Objects.isNull(dbData)) return null;

        return Stream.of(enumClass.getEnumConstants())
            .filter(e -> e.getValue().equals(dbData))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(""));
    }
}
