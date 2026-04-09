package com.daebbang.daebbangapi.domain.category.dto.response;

import com.daebbang.daebbangcore.domain.category.entity.Category;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record CategoryList(
    Long id,
    String categoryName,
    List<CategoryList> children
) {
    public static List<CategoryList> from(List<Category> superCategories, List<Category> allChildren) {
        Map<Long, List<Category>> childrenByParentId = allChildren.stream()
            .collect(Collectors.groupingBy(c -> c.getSuperCategory().getId()));

        return superCategories.stream()
            .map(c -> new CategoryList(
                c.getId(),
                c.getCategoryName(),
                childrenByParentId.getOrDefault(c.getId(), List.of())
                    .stream()
                    .map(child -> new CategoryList(child.getId(), child.getCategoryName(), List.of()))
                    .toList()
            ))
            .toList();
    }
}
