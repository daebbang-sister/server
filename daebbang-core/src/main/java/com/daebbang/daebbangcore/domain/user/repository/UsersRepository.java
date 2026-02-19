package com.daebbang.daebbangcore.domain.user.repository;

import com.daebbang.daebbangcore.domain.user.entity.UserStatus;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsersRepository extends JpaRepository<@NonNull Users,@NonNull Long> {

    @Query("""
    SELECT COUNT(u) > 0
    FROM Users u
    WHERE u.phoneNumber = :phoneNumber
      AND u.status != :excludeStatus
    """)
    boolean existsPhoneNumber(String phoneNumber, UserStatus excludeStatus);

    @Query("""
    SELECT u
    FROM Users u
    WHERE u.status != :excludeStatus
      AND u.loginId = :loginId
    """)
    Optional<Users> findActiveUserByLoginId(String loginId, UserStatus excludeStatus);
}
