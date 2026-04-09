package com.daebbang.daebbangcore.domain.wish.repository;

import com.daebbang.daebbangcore.domain.wish.entity.WishList;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WishListRepository extends JpaRepository<@NonNull WishList, @NonNull Long> {

    @Query("""
    SELECT w
    FROM WishList w
    WHERE w.user.id = :userId
      AND w.product.id = :productId
    """)
    Optional<WishList> findWishListByUserIdAndProductId(
        @Param("userId") Long userId,
        @Param("productId") Long productId
    );
}
