package com.daebbang.daebbangcore.domain.user.repository;

import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.UserStatus;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<@NonNull Users,@NonNull Long> {

    @Query("""
    SELECT COUNT(u) > 0
    FROM Users u
    WHERE u.phoneNumber = :phoneNumber
      AND u.status != :excludeStatus
    """)
    boolean existsPhoneNumber(String phoneNumber, UserStatus excludeStatus);

    @Query("""
    SELECT COUNT(u) > 0
    FROM Users u
    WHERE u.loginId = :loginId
      AND u.status != :excludeStatus
    """)
    boolean existsActiveUser(String loginId, UserStatus excludeStatus);

    @Query("""
    SELECT COUNT(u) > 0
    FROM Users u
    WHERE u.name = :username
      AND u.loginId = :loginId
      AND u.email = :email
      AND u.status != :excludeStatus
    """)
    boolean existsActiveUser(String username, String loginId, String email, UserStatus excludeStatus);

    @Query("""
    SELECT u
    FROM Users u
    WHERE u.loginId = :loginId
      AND u.status != :excludeStatus
      AND u.provider = :provider
    """)
    Optional<Users> findActiveUserByLoginIdAndProvider(String loginId, UserStatus excludeStatus, Provider provider);

    @Query("""
    SELECT u
    FROM Users u
    WHERE u.status != :excludeStatus
      AND u.loginId = :loginId
    """)
    Optional<Users> findActiveUserByLoginId(String loginId, UserStatus excludeStatus);

    @Query("""
    SELECT u
    FROM Users u
    WHERE u.name = :username
      AND u.email = :email
      AND u.status != :excludeStatus
    """)
    List<Users> findActiveUserIdsByUsernameAndEmail(String username, String email, UserStatus excludeStatus);
}
