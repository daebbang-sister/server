package com.daebbang.daebbangcore.domain.user.service;

import com.daebbang.daebbangcore.domain.user.command.MyInfoUpdateCommand;
import com.daebbang.daebbangcore.domain.user.command.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.util.List;

public interface UserService {
    void join(UserJoinCommand joinCommand);
    /**
 * Creates a new user account or updates an existing one using social provider credentials.
 *
 * @param provider     the social login provider
 * @param providerId   the identifier assigned to the user by the provider
 * @param name         the user's display name provided by the social provider
 * @param email        the user's email address provided by the social provider
 * @param phoneNumber  the user's phone number; may be null if not supplied by the provider
 */
void joinOrUpdateSocial(Provider provider, String providerId, String name, String email, String phoneNumber);
    /**
 * Checks whether a user exists with the given phone number.
 *
 * @param phoneNumber the phone number to check for an existing user
 */
void existsByPhoneNumber(String phoneNumber);
    /**
 * Checks whether there are active users associated with the given login identifier.
 *
 * @param loginId the login identifier to check (e.g., username or other login key)
 */
void existsActiveUsers(String loginId);
    /**
 * Initiates sending a phone-number-change authentication code to the specified new phone number.
 *
 * @param userId         the ID of the user requesting the phone number change
 * @param newPhoneNumber the new phone number to verify
 * @return               the authentication code that was sent to the provided phone number
 */
String sendChangePhoneAuthCode(Long userId, String newPhoneNumber);
    /**
 * Issues a temporary password for the user identified by the provided credentials and delivers it to the user's email address.
 *
 * @param username the user's username
 * @param userId the user's login identifier
 * @param email the user's email address where the temporary password will be sent
 */
void issueTemporaryPassword(String username, String userId, String email);
    /**
 * Retrieves the user associated with the specified login identifier.
 *
 * @param loginId the login identifier to look up (e.g., username or login ID)
 * @return the Users entity matching the provided login identifier
 */
Users getUser(String loginId);
    /**
 * Retrieves the Users entity for the given user ID.
 *
 * @param userId numeric identifier of the user
 * @return the Users entity associated with the specified user ID
 */
Users getUserById(Long userId);
    /**
 * Finds users that match the given username and email for login-id lookup.
 *
 * @param username the username (display or account name) to match
 * @param email    the email address to match
 * @return         a list of Users that match the provided username and email; empty if none found
 */
List<Users> getUsersByFindLoginId(String username, String email);
    /**
 * Updates the profile information of the user identified by the given ID.
 *
 * @param userId  the numeric identifier of the user to update
 * @param command contains the profile fields and values to apply to the user
 */
void updateMyInfo(Long userId, MyInfoUpdateCommand command);
    /**
 * Withdraws the user account identified by the given ID.
 *
 * @param userId the numeric identifier of the user to withdraw
 */
void withdraw(Long userId);
}
