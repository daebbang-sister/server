package com.daebbang.daebbangcore.domain.wish.entity;

import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(
    name = "uq_wish_list_user_product",
    columnNames = {"user_id", "product_id"}
))
public class WishList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    private WishList(Users user, Products product) {
        this.user = user;
        this.product = product;
    }

    public static WishList create(Users user, Products product) {
        return new WishList(user, product);
    }
}
