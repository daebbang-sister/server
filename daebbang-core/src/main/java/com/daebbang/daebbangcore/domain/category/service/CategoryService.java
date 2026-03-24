package com.daebbang.daebbangcore.domain.category.service;

import com.daebbang.daebbangcore.domain.category.entity.Category;

public interface CategoryService {
    Category getActiveCategoryById(Long id);
}
