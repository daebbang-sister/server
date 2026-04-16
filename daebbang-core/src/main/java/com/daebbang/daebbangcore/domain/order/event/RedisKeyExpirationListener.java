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

        stockReserveRepository.findByOrderNumber(orderNumber).ifPresent(items -> {
            items.forEach(item ->
                stockCacheService.restoreStock(item.getProductDetailId(), item.getQuantity())
            );
            stockReserveRepository.delete(orderNumber);
            log.info("[StockRestore] keyspace 이벤트로 재고 복구 완료 - orderNumber: {}", orderNumber);
        });
    }
}
