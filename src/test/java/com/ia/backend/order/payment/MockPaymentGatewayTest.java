package com.ia.backend.order.payment;

import com.ia.backend.order.enums.PaymentMethod;
import com.ia.backend.order.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MockPaymentGatewayTest {

    private final MockPaymentGateway gateway = new MockPaymentGateway();

    @Test
    void charge_approvesOnlinePaymentWithMockReference() {
        ReflectionTestUtils.setField(gateway, "enabled", true);
        ReflectionTestUtils.setField(gateway, "declineOnlinePayments", false);

        PaymentResult result = gateway.charge(PaymentMethod.CREDIT_CARD, new BigDecimal("49.99"));

        assertThat(result.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.reference()).startsWith("MOCK-");
        assertThat(result.paidAt()).isNotNull();
    }

    @Test
    void charge_whenDeclineFlagSet_returnsFailed() {
        ReflectionTestUtils.setField(gateway, "enabled", true);
        ReflectionTestUtils.setField(gateway, "declineOnlinePayments", true);

        PaymentResult result = gateway.charge(PaymentMethod.PAYPAL, new BigDecimal("10.00"));

        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.reference()).isNull();
        assertThat(result.paidAt()).isNull();
    }

    @Test
    void charge_whenGatewayDisabled_leavesPaymentPending() {
        ReflectionTestUtils.setField(gateway, "enabled", false);
        ReflectionTestUtils.setField(gateway, "declineOnlinePayments", false);

        PaymentResult result = gateway.charge(PaymentMethod.CREDIT_CARD, new BigDecimal("10.00"));

        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.reference()).isNull();
        assertThat(result.paidAt()).isNull();
    }
}
