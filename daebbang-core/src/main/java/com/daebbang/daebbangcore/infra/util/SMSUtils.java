package com.daebbang.daebbangcore.infra.util;

import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class SMSUtils {
    public static String generateAuthCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    public static String generateAuthMessage(String authCode) {
        return "[대빵언니 인증번호]\n"
            + authCode
            + "\n본인 확인을 위해 인증번호를 입력해주세요.";
    }
}
