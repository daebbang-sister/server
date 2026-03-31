package com.daebbang.daebbangcore.domain.cart.repository;

import com.daebbang.daebbangcore.domain.cart.entity.Carts;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<@NonNull Carts, @NonNull Long> {

    @Query("""
    SELECT c
    FROM Carts c
    JOIN FETCH c.productDetail
    WHERE c.user.id = :userId
      AND c.productDetail.id IN :productDetailIds
    """)
    List<Carts> findByUserIdAndProductDetailIdIn(Long userId, List<Long> productDetailIds);

    @Query("""
    SELECT c FROM Carts c
    WHERE c.id = :cartId
      AND c.user.id = :userId
    """)
    Optional<Carts> findByIdAndUserId(Long cartId, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    DELETE
    FROM Carts c
    WHERE c.id IN :cartIds
      AND c.user.id = :userId
    """)
    void deleteCartsByCartIds(List<Long> cartIds, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    DELETE
    FROM Carts c
    WHERE c.user.id = :userId
    """)
    void deleteAllCartsByUserId(Long userId);
}
