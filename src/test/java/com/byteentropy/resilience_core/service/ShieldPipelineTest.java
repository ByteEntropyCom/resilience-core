package com.byteentropy.resilience_core.service;

import com.byteentropy.resilience_core.client.ExternalBankClient;
import com.byteentropy.resilience_core.model.*;
import com.byteentropy.resilience_core.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ShieldPipelineTest {

    @Autowired private ShieldPipeline pipeline;
    @SpyBean private ExternalBankClient bankClient;

    @Test
    void testIdempotencyPreventsDuplicateCalls() throws Exception {
        String id = UUID.randomUUID().toString();
        PaymentRequest req = new PaymentRequest(id, BigDecimal.TEN, "USD", "M1", "T1", "CARD", "ENC", "REF");
        
        bankClient.setMode(ExternalBankClient.Mode.SUCCESS);

        // Call 1
        pipeline.execute(req).get();
        // Call 2
        pipeline.execute(req).get();

        // Bank should only see ONE request despite TWO pipeline calls
        verify(bankClient, times(1)).call(any());
    }
}
