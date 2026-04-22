package com.daebbang.daebbangcore.domain.product.repository;

import com.daebbang.daebbangcore.domain.product.entity.ProductDetails;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailsRepository extends JpaRepository<@NonNull ProductDetails, @NonNull Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductDetails pd SET pd.stock = pd.stock - :quantity WHERE pd.id = :id AND pd.stock >= :quantity AND :quantity > 0")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductDetails pd SET pd.stock = pd.stock + :quantity WHERE pd.id = :id AND :quantity > 0")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
