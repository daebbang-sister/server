package com.daebbang.daebbangcore.infra.service;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailService {

    private final RedisService redisService;
    private final JavaMailSender emailSender;

    public void senEmail(String email, String title, String content) throws MessagingException {
        try {
            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject(title);
            helper.setText(content, true);
            helper.setFrom("no-reply@daebbang.com", "대빵관리자(회신불가)");
            helper.setReplyTo("noreply@daebbang.com");

            emailSender.send(message);
        } catch (MailAuthenticationException e) {
            log.error("[Email 인증 오류] SMTP 인증 실패 : {}", e.getMessage());
            throw new BusinessException(UserErrorCode.EMAIL_SEND_FAILED);
        } catch (MailSendException e) {
            log.error("[Email 발송 오류] 전송 실패 : {}", e.getMessage());
            throw new BusinessException(UserErrorCode.EMAIL_SEND_FAILED);
        } catch (Exception e) {
            log.error("[Email 기타 오류] 예상치 못한 오류 발생 : {}", e.getMessage());
            throw new BusinessException(UserErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
