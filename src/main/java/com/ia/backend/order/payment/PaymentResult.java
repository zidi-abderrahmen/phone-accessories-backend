package com.ia.backend.order.payment;

import com.ia.backend.order.enums.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResult(
        PaymentStatus status,
        String reference,
        LocalDateTime paidAt
) {

    public static PaymentResult pending() {
        return new PaymentResult(PaymentStatus.PENDING, null, null);
    }

    public static PaymentResult failed() {
        return new PaymentResult(PaymentStatus.FAILED, null, null);
    }

    public static PaymentResult paid(String reference) {
        return new PaymentResult(PaymentStatus.PAID, reference, LocalDateTime.now());
    }
}
