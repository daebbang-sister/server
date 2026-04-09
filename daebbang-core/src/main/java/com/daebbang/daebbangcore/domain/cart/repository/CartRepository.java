package com.daebbang.daebbangcore.domain.cart.repository;

import com.daebbang.daebbangcore.domain.cart.entity.Carts;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<@NonNull Carts, @NonNull Long> {

    @Query("""
    SELECT c
    FROM Carts c
    JOIN FETCH c.productDetail pd
    JOIN FETCH pd.product p
    WHERE c.user.id = :userId
      AND (:cursor IS NULL OR c.id < :cursor)
    ORDER BY c.id DESC
    """)
    List<Carts> findCartsByUserIdWithCursor(
        @Param("userId") Long userId,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    @Query("""
    SELECT c
    FROM Carts c
    JOIN FETCH c.productDetail
    WHERE c.user.id = :userId
      AND c.productDetail.id IN :productDetailIds
    """)
    List<Carts> findByUserIdAndProductDetailIdIn(
        @Param("userId") Long userId,
        @Param("productDetailIds") List<Long> productDetailIds
    );

    @Query("""
    SELECT c FROM Carts c
    WHERE c.id = :cartId
      AND c.user.id = :userId
    """)
    Optional<Carts> findByIdAndUserId(
        @Param("cartId") Long cartId,
        @Param("userId") Long userId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    DELETE
    FROM Carts c
    WHERE c.id IN :cartIds
      AND c.user.id = :userId
    """)
    void deleteCartsByCartIds(
        @Param("cartIds") List<Long> cartIds,
        @Param("userId") Long userId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    DELETE
    FROM Carts c
    WHERE c.user.id = :userId
    """)
    void deleteAllCartsByUserId(@Param("userId") Long userId);
}
