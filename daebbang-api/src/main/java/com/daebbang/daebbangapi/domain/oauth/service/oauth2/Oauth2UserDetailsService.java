package com.daebbang.daebbangapi.domain.oauth.service.oauth2;

import com.daebbang.daebbangapi.domain.oauth.dto.response.oauth2.OAuth2Response;
import com.daebbang.daebbangapi.domain.oauth.factory.OAuth2ResponseFactoryImpl;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.factory.OAuth2ResponseFactory;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Oauth2UserDetailsService extends DefaultOAuth2UserService {

    private final OAuth2ResponseFactory oAuth2ResponseFactory;

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest
                                            .getClientRegistration()
                                            .getRegistrationId();

        log.info("Oauth2 요청 플랫폼 : {} - Provider Id : {}", registrationId, oAuth2User.getAttributes().get("id"));

        OAuth2Response user = getOAuth2Response(registrationId, oAuth2User);

        userService.joinOrUpdateSocial(
            user.getProvider(),
            user.getProviderId(),
            user.getUseEmail(),
            user.getUsername(),
            user.getPhoneNumber());

        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                user.getAttributeKeyName());
    }

    private OAuth2Response getOAuth2Response(String registrationId, OAuth2User user) {
        Provider provider = Provider.findByRegistrationId(registrationId);
        return (OAuth2Response) oAuth2ResponseFactory.createResponse(provider, user.getAttributes());
    }
}
