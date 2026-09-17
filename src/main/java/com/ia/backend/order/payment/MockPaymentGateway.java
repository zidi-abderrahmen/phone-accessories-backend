package com.ia.backend.order.payment;

import com.ia.backend.order.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stand-in for a real payment provider. It never contacts an external service:
 * online payments are approved immediately and stamped with a mock reference.
 * Swap this bean for a real gateway implementation when moving off the demo.
 */
@Slf4j
@Component
public class MockPaymentGateway {

    @Value("${application.payment.mock.enabled:true}")
    private boolean enabled;

    @Value("${application.payment.mock.decline-online-payments:false}")
    private boolean declineOnlinePayments;

    public PaymentResult charge(PaymentMethod method, BigDecimal amount) {
        if (!enabled) {
            log.warn("Mock payment gateway is disabled; leaving payment pending (method={})", method);
            return PaymentResult.pending();
        }

        if (declineOnlinePayments) {
            log.warn("Mock payment declined (method={}, amount={})", method, amount);
            return PaymentResult.failed();
        }

        String reference = "MOCK-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        log.info("Mock payment approved (method={}, amount={}, reference={})",
                method, amount, reference);

        return PaymentResult.paid(reference);
    }
}
