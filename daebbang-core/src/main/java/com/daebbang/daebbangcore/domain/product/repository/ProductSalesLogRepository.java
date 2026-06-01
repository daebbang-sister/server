package com.daebbang.daebbangcore.domain.product.repository;

import com.daebbang.daebbangcore.domain.product.entity.ProductSalesLog;
import java.time.LocalDate;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSalesLogRepository extends JpaRepository<@NonNull ProductSalesLog, @NonNull Long> {

    @Modifying
    @Query(
        value = "INSERT INTO product_sales_log (product_id, sale_date, sales_count) " +
                "VALUES (:productId, :saleDate, :delta) " +
                "ON DUPLICATE KEY UPDATE sales_count = sales_count + :delta",
        nativeQuery = true
    )
    void upsertSalesLog(
        @Param("productId") Long productId,
        @Param("saleDate") LocalDate saleDate,
        @Param("delta") long delta
    );

    @Modifying
    @Query("DELETE FROM ProductSalesLog psl WHERE psl.saleDate < :threshold")
    int deleteBySaleDateBefore(@Param("threshold") LocalDate threshold);
}
