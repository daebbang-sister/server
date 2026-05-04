package com.daebbang.daebbangcore.infra.service;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.SmsErrorCode;
import com.daebbang.daebbangcore.infra.util.SMSUtils;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${spring.sms.sender}")
    private String sender;

    private final static String SMS_KEY = "SMS:";

    private final RedisService redisService;
    private final DefaultMessageService messageService;

    public void sendAuthMessage(String phoneNumber) {
        String authCode = SMSUtils.generateAuthCode();
        String authMessage = SMSUtils.generateAuthMessage(authCode);

        Message message = new Message();
        message.setFrom(sender);
        message.setTo(phoneNumber);
        message.setText(authMessage);

        try {
            messageService.send(message);
            log.info("[SMS] 인증번호 발송 - to: {}, code: {}", phoneNumber, authCode);
        } catch (SolapiMessageNotReceivedException e) {
            log.error("[SMS] 인증번호 발송 실패 - to: {}, 발생한 에러 목록: {}, 에러: {}", phoneNumber, e.getFailedMessageList(), e.getMessage(), e);
            throw new BusinessException(SmsErrorCode.SMS_SEND_FAILED);
        } catch (Exception e) {
            log.error("[SMS] 인증번호 발송 실패 - to: {}, 에러: {}", phoneNumber, e.getMessage(), e);
            throw new BusinessException(SmsErrorCode.SMS_SEND_FAILED);
        }

        redisService.setData(SMS_KEY + phoneNumber, authCode, Duration.ofMinutes(5));
    }

    public void verifyAuthCode(String phoneNumber, String authCode) {
        String savedCode = redisService.getData(SMS_KEY + phoneNumber);
        if (Objects.isNull(savedCode)) {
            log.warn("[SMS] 인증 실패 - 만료되었거나 발송 이력 없음: {}", phoneNumber);
            throw new BusinessException(SmsErrorCode.AUTH_CODE_EXPIRED);
        }
        if (!savedCode.equalsIgnoreCase(authCode)) {
            log.warn("[SMS] 인증 실패 - 코드 불일치: input:{}, saved:{}", authCode, savedCode);
            throw new BusinessException(SmsErrorCode.AUTH_CODE_MISMATCH);
        }
        redisService.setData(SMS_KEY + phoneNumber, "DONE", Duration.ofMinutes(10));
        log.info("[SMS] 인증 완료 - phoneNumber: {}", phoneNumber);
    }

    public boolean isVerified(String phoneNumber) {
        String status = redisService.getData(SMS_KEY + phoneNumber);
        return "DONE".equals(status);
    }

    public void deleteVerification(String phoneNumber) {
        redisService.deleteData(SMS_KEY + phoneNumber);
    }
}
