package com.daebbang.daebbangcore.domain.product.service;

import com.daebbang.daebbangcore.domain.product.entity.ProductDetails;

public interface ProductDetailsService {

    ProductDetails getProductDetailsById(Long id);

    int decreaseStock(Long id, int quantity);
}
