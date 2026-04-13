package com.daebbang.daebbangcore.domain.wish.repository.dsl;

import com.daebbang.daebbangcore.domain.wish.dto.WishListQueryResult;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishListCustomRepository {

    Page<@NonNull WishListQueryResult> findWishListPageByUserId(Long userId, Pageable pageable);
}
