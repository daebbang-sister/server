package com.daebbang.daebbangcore.domain.product.repository;

import com.daebbang.daebbangcore.domain.product.entity.Products;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<@NonNull Products, @NonNull Long> {

}
