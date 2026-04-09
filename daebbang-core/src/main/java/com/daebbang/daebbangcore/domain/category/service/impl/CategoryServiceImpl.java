package com.daebbang.daebbangcore.domain.category.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.category.dto.CategoryHierarchy;
import com.daebbang.daebbangcore.domain.category.entity.Category;
import com.daebbang.daebbangcore.domain.category.repository.CategoryRepository;
import com.daebbang.daebbangcore.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category getActiveCategoryById(Long id) {
        return categoryRepository.findCategoryById(id)
            .orElseThrow(() -> new BusinessException(UserErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public CategoryHierarchy getCategoryHierarchy() {
        return CategoryHierarchy.from(categoryRepository.findAllCategories());
    }
}
