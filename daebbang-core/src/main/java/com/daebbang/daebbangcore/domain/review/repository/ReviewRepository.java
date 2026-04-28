package com.daebbang.daebbangcore.domain.review.repository;

import com.daebbang.daebbangcore.domain.review.entity.Review;
import com.daebbang.daebbangcore.domain.review.repository.dsl.ReviewCustomRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewCustomRepository {

    @Query("""
        SELECT r FROM Review r
        WHERE r.id = :id
          AND r.deletedAt IS NULL
        """)
    Optional<Review> findActiveById(@Param("id") Long id);

    @Query("""
        SELECT r FROM Review r
        WHERE r.id = :id
          AND r.user.id = :userId
          AND r.deletedAt IS NULL
        """)
    Optional<Review> findActiveByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    boolean existsByOrderDetailIdAndDeletedAtIsNull(Long orderDetailId);
}
