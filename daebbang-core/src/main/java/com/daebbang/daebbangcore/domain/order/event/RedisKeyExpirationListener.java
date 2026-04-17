package com.daebbang.daebbangcore.domain.order.event;

import com.daebbang.daebbangcore.domain.order.service.StockCacheService;
import com.daebbang.daebbangcore.domain.order.session.OrderStockReserveRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private static final String SESSION_PREFIX = "order:session:";

    private final OrderStockReserveRedisRepository stockReserveRepository;
    private final StockCacheService stockCacheService;

    public RedisKeyExpirationListener(
            RedisMessageListenerContainer listenerContainer,
            OrderStockReserveRedisRepository stockReserveRepository,
            StockCacheService stockCacheService) {
        super(listenerContainer);
        this.stockReserveRepository = stockReserveRepository;
        this.stockCacheService = stockCacheService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());

        if (!expiredKey.startsWith(SESSION_PREFIX)) return;

        String orderNumber = expiredKey.substring(SESSION_PREFIX.length());
        log.info("[StockRestore] 세션 만료 감지 - orderNumber: {}", orderNumber);

        try {
            stockReserveRepository.findByOrderNumber(orderNumber).ifPresent(items -> {
                // 스케줄러와의 중복 복원 방지를 위해 예약 키 먼저 삭제
                stockReserveRepository.delete(orderNumber);
                for (var item : items) {
                    try {
                        stockCacheService.restoreStock(item.getProductDetailId(), item.getQuantity());
                    } catch (Exception e) {
                        log.error("[StockRestore] 재고 복원 실패 - productDetailId: {}, quantity: {}",
                            item.getProductDetailId(), item.getQuantity(), e);
                    }
                }
                log.info("[StockRestore] keyspace 이벤트로 재고 복구 완료 - orderNumber: {}", orderNumber);
            });
        } catch (Exception e) {
            log.error("[StockRestore] 세션 만료 처리 중 오류 - orderNumber: {} (스케줄러 안전망이 처리)", orderNumber, e);
        }
    }
}
