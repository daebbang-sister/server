package com.daebbang.daebbangcore.domain.wish.service;

import com.daebbang.daebbangcore.domain.wish.dto.WishListQueryResult;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishListService {

    Page<@NonNull WishListQueryResult> getWishList(Long userId, Pageable pageable);

    boolean isWished(Long userId, Long productId);

    Long addWishList(Long userId, Long productId);

    void deleteWishLists(Long userId, List<Long> wishListIds);

    void deleteAllWishLists(Long userId);
}
