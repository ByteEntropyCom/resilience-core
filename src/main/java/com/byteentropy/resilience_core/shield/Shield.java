package com.byteentropy.resilience_core.shield;

import com.byteentropy.resilience_core.model.PaymentRequest;
import com.byteentropy.resilience_core.model.PaymentResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Shield {
    CompletableFuture<PaymentResponse> apply(
        PaymentRequest request,
        Supplier<CompletableFuture<PaymentResponse>> next
    );
}