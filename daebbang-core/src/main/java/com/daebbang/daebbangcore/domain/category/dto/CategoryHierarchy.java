package com.daebbang.daebbangcore.domain.category.dto;

import com.daebbang.daebbangcore.domain.category.entity.Category;
import java.util.List;

public record CategoryHierarchy(
    List<Category> superCategories,
    List<Category> children
) {
    public static CategoryHierarchy from(List<Category> all) {
        return new CategoryHierarchy(
            all.stream().filter(c -> c.getSuperCategory() == null).toList(),
            all.stream().filter(c -> c.getSuperCategory() != null).toList()
        );
    }
}
