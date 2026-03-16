package com.daebbang.daebbangcore.domain.product.repository;

import com.daebbang.daebbangcore.domain.product.entity.ProductCategory;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<@NonNull ProductCategory, @NonNull Long> {

}
