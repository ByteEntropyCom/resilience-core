package com.byteentropy.resilience_core.api;

import com.byteentropy.resilience_core.model.PaymentRequest;
import com.byteentropy.resilience_core.model.PaymentResponse;
import com.byteentropy.resilience_core.service.ShieldPipeline;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final ShieldPipeline pipeline;

    public PaymentController(ShieldPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<PaymentResponse>> pay(
            @Valid @RequestBody PaymentRequest req) {
        return pipeline.execute(req)
                .thenApply(ResponseEntity::ok);
    }
}