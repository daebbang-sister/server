package com.daebbang.daebbangcore.domain.product.service.impl;

import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import com.daebbang.daebbangcore.domain.product.repository.ProductRepository;
import com.daebbang.daebbangcore.domain.product.service.ProductService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final int NEW_PRODUCT_DAYS = 30;

    private final ProductRepository productRepository;

    @Override
    public List<ProductCardQueryResult> getNewProductsOnSale(int limit) {

        LocalDate from = LocalDate.now().minusDays(NEW_PRODUCT_DAYS);

        return productRepository.findNewProductsOnSale(from, limit);
    }

    @Override
    public List<ProductCardQueryResult> getCategoryProductsOnSale(Long categoryId, int limit) {
        return productRepository.findCategoryProductsOnSale(categoryId, limit);
    }
}
