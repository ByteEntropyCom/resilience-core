package com.byteentropy.resilience_core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_history")
public class PaymentEntity {
    @Id
    private String idempotencyId;
    private String status;
    private String gatewayTxnId;
    private String message;
    private LocalDateTime createdAt = LocalDateTime.now();

    public PaymentEntity() {}

    public PaymentEntity(String id, String status, String txnId, String msg) {
        this.idempotencyId = id;
        this.status = status;
        this.gatewayTxnId = txnId;
        this.message = msg;
    }

    // Getters and Setters
    public String getIdempotencyId() { return idempotencyId; }
    public String getStatus() { return status; }
    public String getGatewayTxnId() { return gatewayTxnId; }
    public String getMessage() { return message; }
}
