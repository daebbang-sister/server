package com.daebbang.daebbangcore.infra.converter;

import com.daebbang.daebbangcore.domain.product.entity.ProductStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductStatusConverter extends AbstractBaseEnumConverter<ProductStatus, Integer> {

    public ProductStatusConverter() {
        super(ProductStatus.class);
    }
}
