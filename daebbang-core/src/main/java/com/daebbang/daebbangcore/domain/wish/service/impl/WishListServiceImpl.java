package com.daebbang.daebbangcore.domain.wish.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.WishErrorCode;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.product.service.ProductService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.domain.wish.dto.WishListQueryResult;
import com.daebbang.daebbangcore.domain.wish.entity.WishList;
import com.daebbang.daebbangcore.domain.wish.repository.WishListRepository;
import com.daebbang.daebbangcore.domain.wish.service.WishListService;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
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
    public Page<@NonNull WishListQueryResult> getWishList(Long userId, Pageable pageable) {
        return wishListRepository.findWishListPageByUserId(userId, pageable);
    }

    @Override
    public Optional<Long> isWished(Long userId, Long productId) {
        return wishListRepository.findWishListByUserIdAndProductId(userId, productId).map(WishList::getId);
    }

    @Override
    @Transactional
    public Long addWishList(Long userId, Long productId) {
        wishListRepository.findWishListByUserIdAndProductId(userId, productId)
            .ifPresent(w -> { throw new BusinessException(WishErrorCode.WISH_LIST_ALREADY_EXISTS); });

        Users user = userService.getUserById(userId);
        Products product = productService.getOnSaleProductById(productId);
        return wishListRepository.save(WishList.create(user, product)).getId();
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
