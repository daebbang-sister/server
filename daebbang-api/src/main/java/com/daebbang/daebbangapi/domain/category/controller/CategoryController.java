package com.daebbang.daebbangapi.domain.category.controller;

import com.daebbang.daebbangapi.domain.category.dto.response.CategoryList;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcore.domain.category.dto.CategoryHierarchy;
import com.daebbang.daebbangcore.domain.category.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public CommonResponse<List<CategoryList>> getCategories() {
        CategoryHierarchy hierarchy = categoryService.getCategoryHierarchy();
        return CommonResponse.success(CommonSuccessCode.SELECT_SUCCESS,
            CategoryList.from(hierarchy.superCategories(), hierarchy.children()));
    }
}
