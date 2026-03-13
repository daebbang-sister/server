package com.daebbang.daebbangcore.infra.converter;

import com.daebbang.daebbangcore.domain.product.entity.DiscountType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DiscountTypeConverter extends AbstractBaseEnumConverter<DiscountType, Integer> {

    protected DiscountTypeConverter() {
        super(DiscountType.class);
    }
}
