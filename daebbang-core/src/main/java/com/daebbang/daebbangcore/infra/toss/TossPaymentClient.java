package com.daebbang.daebbangcore.infra.toss;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.infra.toss.dto.TossConfirmRequest;
import com.daebbang.daebbangcore.infra.toss.dto.TossPaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    private final ObjectMapper objectMapper;

    public TossPaymentResponse confirm(String orderId, String paymentKey, int amount) {
        try {
            String body = objectMapper.writeValueAsString(
                new TossConfirmRequest(orderId, paymentKey, amount)
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/v1/payments/confirm"))
                .header("Authorization", basicAuth())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("[Toss] 결제 승인 실패 - orderId: {}, status: {}",
                    orderId, response.statusCode());
                throw new BusinessException(UserErrorCode.PAYMENT_CONFIRMATION_FAILED);
            }

            return objectMapper.readValue(response.body(), TossPaymentResponse.class);

        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(UserErrorCode.PAYMENT_CONFIRMATION_FAILED);
        } catch (Exception e) {
            log.error("[Toss] 결제 승인 중 예외 - orderId: {}, error: {}", orderId, e.getMessage(), e);
            throw new BusinessException(UserErrorCode.PAYMENT_CONFIRMATION_FAILED);
        }
    }


    public void cancel(String paymentKey, String cancelReason) {
        cancel(paymentKey, cancelReason, null);
    }


    public void cancel(String paymentKey, String cancelReason, Integer cancelAmount) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("cancelReason", cancelReason);
            if (cancelAmount != null) {
                payload.put("cancelAmount", cancelAmount);
            }
            String body = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/v1/payments/" + paymentKey + "/cancel"))
                .header("Authorization", basicAuth())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("[Toss] 결제 취소 실패 - paymentKey: {}, status: {}",
                    paymentKey, response.statusCode());
                throw new BusinessException(UserErrorCode.PAYMENT_CANCEL_FAILED);
            }

        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(UserErrorCode.PAYMENT_CANCEL_FAILED);
        } catch (Exception e) {
            log.error("[Toss] 결제 취소 중 예외 - paymentKey: {}, error: {}", paymentKey, e.getMessage(), e);
            throw new BusinessException(UserErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }

    private String basicAuth() {
        String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        return "Basic " + encoded;
    }
}
