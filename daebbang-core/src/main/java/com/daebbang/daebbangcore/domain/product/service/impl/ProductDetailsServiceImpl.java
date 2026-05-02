package com.daebbang.daebbangcore.domain.product.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.ProductErrorCode;
import com.daebbang.daebbangcore.domain.product.entity.ProductDetails;
import com.daebbang.daebbangcore.domain.product.repository.ProductDetailsRepository;
import com.daebbang.daebbangcore.domain.product.service.ProductDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDetailsServiceImpl implements ProductDetailsService {

    private final ProductDetailsRepository productDetailsRepository;

    @Override
    public ProductDetails getProductDetailsById(Long id) {
        return productDetailsRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_DETAILS_NOT_FOUND));
    }

    @Override
    @Transactional
    public int decreaseStock(Long id, int quantity) {
        return productDetailsRepository.decreaseStock(id, quantity);
    }

    @Override
    @Transactional
    public int increaseStock(Long id, int quantity) {
        return productDetailsRepository.increaseStock(id, quantity);
    }
}
