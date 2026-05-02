package com.daebbang.daebbangcore.infra.service;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.EmailErrorCode;
import com.daebbang.daebbangcore.infra.util.EmailUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailService {

    private final JavaMailSender emailSender;
    private final ResourceLoader resourceLoader;

    @Async
    public void sendTemporaryPassword(String email) {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/mail/email_temporary_password.html");
            InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            String content = FileCopyUtils.copyToString(reader);

            content = content.replace("{temporary_password}", EmailUtils.generateTemporaryPassword());
            sendEmail(email, "[대빵언니] 임시 비밀번호 안내입니다.", content);
        } catch (Exception e) {
            log.error("[Email 템플릿 읽기 오류] : {}", e.getMessage());
            throw new BusinessException(EmailErrorCode.EMAIL_SEND_FAILED);
        }
    }

    public void sendEmail(String email, String title, String content) throws MessagingException {
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
            throw new BusinessException(EmailErrorCode.EMAIL_SEND_FAILED);
        } catch (MailSendException e) {
            log.error("[Email 발송 오류] 전송 실패 : {}", e.getMessage());
            throw new BusinessException(EmailErrorCode.EMAIL_SEND_FAILED);
        } catch (Exception e) {
            log.error("[Email 기타 오류] 예상치 못한 오류 발생 : {}", e.getMessage());
            throw new BusinessException(EmailErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
