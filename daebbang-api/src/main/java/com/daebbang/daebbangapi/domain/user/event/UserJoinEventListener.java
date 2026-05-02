package com.daebbang.daebbangapi.domain.user.event;

import com.daebbang.daebbangcore.domain.user.event.UserJoinEvent;
import com.daebbang.daebbangcore.infra.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserJoinEventListener {

    private final SmsService smsService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserJoinedEvent(UserJoinEvent event) {
        log.info("[Event] 회원가입 완료 - 인증 데이터 정리 시작: {}", event.phoneNumber());
        try {
            smsService.deleteVerification(event.phoneNumber());
        } catch (Exception e) {
            // 이벤트 처리는 실패해도 메인 로직엔 지장이 없어야 하므로 로그만 남김
            log.error("[Event] 인증 데이터 삭제 중 오류 발생: {}", e.getMessage());
        }
    }
}
