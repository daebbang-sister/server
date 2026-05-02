package com.daebbang.daebbangapi.domain.oauth.factory;

import com.daebbang.daebbangapi.domain.oauth.provider.OAuth2ResponseProvider;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.OAuth2ErrorCode;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.factory.OAuth2ResponseFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2ResponseFactoryImpl implements OAuth2ResponseFactory {

    private final List<OAuth2ResponseProvider> providers;

    @Override
    public Object createResponse(Provider provider, Map<String, Object> attributes) {
        return providers.stream()
            .filter(p -> p.supports(provider))
            .findFirst()
            .map(p -> p.create(attributes))
            .orElseThrow(() -> new BusinessException(OAuth2ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER));
    }
}
