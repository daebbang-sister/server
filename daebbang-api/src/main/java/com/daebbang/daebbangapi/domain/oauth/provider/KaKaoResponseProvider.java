package com.daebbang.daebbangapi.domain.oauth.provider;

import com.daebbang.daebbangapi.domain.oauth.dto.response.oauth2.KaKaoResponse;
import com.daebbang.daebbangapi.domain.oauth.dto.response.oauth2.OAuth2Response;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class KaKaoResponseProvider implements OAuth2ResponseProvider {

    @Override
    public boolean supports(Provider provider) {
        return provider.equals(Provider.KAKAO);
    }

    @Override
    public OAuth2Response create(Map<String, Object> attributes) {
        return new KaKaoResponse(attributes);
    }
}
