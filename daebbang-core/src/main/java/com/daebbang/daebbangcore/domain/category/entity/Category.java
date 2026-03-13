package com.daebbang.daebbangcore.domain.category.entity;

import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_id")
    private Category superCategory;

    @Column(nullable = false, length = 50)
    private String categoryName;

    @Builder
    private Category(Category superCategory, String categoryName) {
        this.superCategory = superCategory;
        this.categoryName = categoryName;
    }

    public static Category create(Category superCategory, String categoryName) {
        return Category.builder()
                        .superCategory(superCategory)
                        .categoryName(categoryName)
                        .build();
    }
}
