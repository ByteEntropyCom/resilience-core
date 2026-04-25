package com.byteentropy.resilience_core.model;

public record PaymentResponse(
        String idempotencyId,
        String gatewayTxnId,
        String status,
        String authCode,
        String message
) {
    public static PaymentResponse of(String id, String status, String msg) {
        return new PaymentResponse(id, null, status, null, msg);
    }
}