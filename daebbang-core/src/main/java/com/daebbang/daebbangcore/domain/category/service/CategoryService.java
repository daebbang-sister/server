package com.daebbang.daebbangcore.domain.category.service;

import com.daebbang.daebbangcore.domain.category.entity.Category;
import java.util.List;

public interface CategoryService {
    Category getActiveCategoryById(Long id);
    List<Category> getAllCategories();
}
