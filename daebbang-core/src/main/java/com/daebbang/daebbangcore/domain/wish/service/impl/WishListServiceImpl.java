package com.daebbang.daebbangcore.domain.wish.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.product.service.ProductService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.domain.wish.dto.WishListQueryResult;
import com.daebbang.daebbangcore.domain.wish.entity.WishList;
import com.daebbang.daebbangcore.domain.wish.repository.WishListRepository;
import com.daebbang.daebbangcore.domain.wish.service.WishListService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;
    private final UserService userService;
    private final ProductService productService;

    @Override
    public Page<WishListQueryResult> getWishList(Long userId, Pageable pageable) {
        return wishListRepository.findWishListPageByUserId(userId, pageable);
    }

    @Override
    public boolean isWished(Long userId, Long productId) {
        return wishListRepository.findWishListByUserIdAndProductId(userId, productId).isPresent();
    }

    @Override
    @Transactional
    public void addWishList(Long userId, Long productId) {
        // 중복 추가 방지 (데이터 정합성)
        wishListRepository.findWishListByUserIdAndProductId(userId, productId)
            .ifPresent(w -> { throw new BusinessException(UserErrorCode.WISH_LIST_ALREADY_EXISTS); });

        Users user = userService.getUserById(userId);
        Products product = productService.getOnSaleProductById(productId);
        wishListRepository.save(WishList.create(user, product));
    }

    @Override
    @Transactional
    public void deleteWishLists(Long userId, List<Long> wishListIds) {
        // userId 조건 포함 → 타인의 위시리스트 삭제 방지 (데이터 정합성)
        wishListRepository.deleteByIdsAndUserId(wishListIds, userId);
    }

    @Override
    @Transactional
    public void deleteAllWishLists(Long userId) {
        wishListRepository.deleteAllByUserId(userId);
    }
}
