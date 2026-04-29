package com.daebbang.daebbangcore.domain.address.repository;

import com.daebbang.daebbangcore.domain.address.entity.Address;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<@NonNull Address, @NonNull Long> {

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true AND a.deletedAt IS NULL")
    Optional<Address> findDefaultByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.deletedAt IS NULL")
    List<Address> findAllActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId AND a.deletedAt IS NULL")
    int countActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Address a WHERE a.id = :addressId AND a.user.id = :userId AND a.deletedAt IS NULL")
    Optional<Address> findByIdAndUserIdActive(@Param("addressId") Long addressId, @Param("userId") Long userId);
}
