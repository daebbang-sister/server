package com.daebbang.daebbangcore.infra.service;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.infra.util.SMSUtils;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import java.time.Duration;
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
            throw new BusinessException(UserErrorCode.SMS_SEND_FAILED);
        } catch (Exception e) {
            log.error("[SMS] 인증번호 발송 실패 - to: {}, 에러: {}", phoneNumber, e.getMessage(), e);
            throw new BusinessException(UserErrorCode.SMS_SEND_FAILED);
        }

        redisService.setData("SMS", authCode, Duration.ofMinutes(5));
    }
}
