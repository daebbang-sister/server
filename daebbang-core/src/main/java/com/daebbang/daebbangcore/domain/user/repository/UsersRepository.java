package com.daebbang.daebbangcore.domain.user.repository;

import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.UserStatus;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<@NonNull Users, @NonNull Long> {

    /**
     * Checks whether any user exists with the given phone number while excluding users with a specific status.
     *
     * @param phoneNumber   the phone number to search for
     * @param excludeStatus the user status to exclude from the search
     * @return              `true` if at least one matching user exists, `false` otherwise
     */
    @Query("""
    SELECT COUNT(u) > 0
    FROM Users u
    WHERE u.phoneNumber = :phoneNumber
      AND u.status != :excludeStatus
    """)
    boolean existsPhoneNumber(
        @Param("phoneNumber") String phoneNumber,
        @Param("excludeStatus") UserStatus excludeStatus
    );

    /**
     * Determines whether any user other than the specified user has the given phone number while excluding users with the provided status.
     *
     * @param phoneNumber the phone number to check
     * @param excludeUserId the ID of the user to exclude from the check
     * @param excludeStatus the user status value to exclude from matching
     * @return `true` if at least one matching user exists, `false` otherwise
     */
    @Query("""
    SELECT COUNT(u) > 0
    FROM Users u
    WHERE u.phoneNumber = :phoneNumber
      AND u.id <> :excludeUserId
      AND u.status != :excludeStatus
    """)
    boolean existsPhoneNumberExcludingSelf(
        @Param("phoneNumber") String phoneNumber,
        @Param("excludeUserId") Long excludeUserId,
        @Param("excludeStatus") UserStatus excludeStatus
    );

    /**
     * Determines whether a user with the given login ID exists and does not have the specified excluded status.
     *
     * @param loginId the login identifier to search for
     * @param excludeStatus the user status value to exclude from the existence check
     * @return `true` if at least one user with `loginId` exists whose status is not equal to `excludeStatus`, `false` otherwise
     */
    @Query("""
    SELECT COUNT(u) > 0
    FROM Users u
    WHERE u.loginId = :loginId
      AND u.status != :excludeStatus
    """)
    boolean existsActiveUser(
        @Param("loginId") String loginId,
        @Param("excludeStatus") UserStatus excludeStatus
    );

    @Query("""
    SELECT COUNT(u) > 0
    FROM Users u
    WHERE u.name = :username
      AND u.loginId = :loginId
      AND u.email = :email
      AND u.status != :excludeStatus
    """)
    boolean existsActiveUser(
        @Param("username") String username,
        @Param("loginId") String loginId,
        @Param("email") String email,
        @Param("excludeStatus") UserStatus excludeStatus
    );

    @Query("""
    SELECT u
    FROM Users u
    WHERE u.loginId = :loginId
      AND u.status != :excludeStatus
      AND u.provider = :provider
    """)
    Optional<Users> findActiveUserByLoginIdAndProvider(
        @Param("loginId") String loginId,
        @Param("excludeStatus") UserStatus excludeStatus,
        @Param("provider") Provider provider
    );

    @Query("""
    SELECT u
    FROM Users u
    WHERE u.status != :excludeStatus
      AND u.loginId = :loginId
    """)
    Optional<Users> findActiveUserByLoginId(
        @Param("loginId") String loginId,
        @Param("excludeStatus") UserStatus excludeStatus
    );

    @Query("""
    SELECT u
    FROM Users u
    WHERE u.name = :username
      AND u.email = :email
      AND u.status != :excludeStatus
    """)
    List<Users> findActiveUserIdsByUsernameAndEmail(
        @Param("username") String username,
        @Param("email") String email,
        @Param("excludeStatus") UserStatus excludeStatus
    );

    @Query("""
    SELECT u
    FROM Users u
    WHERE u.name = :username
      AND u.loginId = :loginId
      AND u.email = :email
      AND u.status != :excludeStatus
    """)
    Optional<Users> findActiveUser(
        @Param("username") String username,
        @Param("loginId") String loginId,
        @Param("email") String email,
        @Param("excludeStatus") UserStatus excludeStatus
    );
}
