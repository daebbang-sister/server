package com.daebbang.daebbangcore.domain.product.repository;

import com.daebbang.daebbangcore.domain.product.entity.ProductStatus;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.product.repository.dsl.ProductCustomRepository;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<@NonNull Products, @NonNull Long>,
    ProductCustomRepository {

    @Query("""
    SELECT p FROM Products p
    WHERE p.id = :id
      AND p.productStatus = :status
      AND p.deletedAt IS NULL
    """)
    Optional<Products> findByIdAndStatusAndNotDeleted(
        @Param("id") Long id,
        @Param("status") ProductStatus status
    );
}
