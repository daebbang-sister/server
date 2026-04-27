package com.daebbang.daebbangcore.infra.converter;

import com.daebbang.daebbangcore.domain.review.entity.ReviewPointStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReviewPointStatusConverter extends AbstractBaseEnumConverter<ReviewPointStatus, Integer> {

    public ReviewPointStatusConverter() {
        super(ReviewPointStatus.class);
    }
}
