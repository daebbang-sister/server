package com.daebbang.daebbangcore.infra.config;

import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoolSMSConfig {

    @Value("${spring.sms.client-id}")
    private String apiKey;

    @Value("${spring.sms.client-secret}")
    private String secretKey;

    @Bean
    public DefaultMessageService messageService() {
        return new DefaultMessageService(apiKey, secretKey, "https://api.coolsms.co.kr");
    }
}
