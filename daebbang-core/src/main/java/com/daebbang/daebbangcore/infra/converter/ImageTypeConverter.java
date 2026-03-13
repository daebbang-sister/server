package com.daebbang.daebbangcore.infra.converter;

import com.daebbang.daebbangcore.domain.product.entity.ImageType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ImageTypeConverter extends AbstractBaseEnumConverter<ImageType, Integer> {

    public ImageTypeConverter() {
        super(ImageType.class);
    }
}
