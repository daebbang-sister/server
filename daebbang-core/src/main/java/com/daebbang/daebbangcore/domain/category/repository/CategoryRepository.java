package com.daebbang.daebbangcore.domain.category.repository;

import com.daebbang.daebbangcore.domain.category.entity.Category;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
    SELECT c
    FROM Category c
    WHERE c.deletedAt is NULL
      AND c.id = :id
    """)
    Optional<Category> findCategoryById(@Param("id") Long id);

    @Query("""
    SELECT c
    FROM Category c
    LEFT JOIN FETCH c.superCategory
    WHERE c.deletedAt IS NULL
    """)
    List<Category> findAllCategories();
}
