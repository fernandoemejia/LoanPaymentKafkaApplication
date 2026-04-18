package com.loanpayment.service;

import com.loanpayment.model.LoanPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    private LoanPayment loanPayment;

    @BeforeEach
    void setUp() {
        loanPayment = LoanPayment.builder()
                .accountNumber("12345678")
                .dueAmount(new BigDecimal("500.00"))
                .status("PENDING")
                .paymentId("test-payment-123")
                .build();
    }

    @Test
    void testProcessPaymentSuccess() {
        // Initialize account
        paymentService.initializeAccountBalance("12345678", new BigDecimal("10000.00"));

        // Process payment
        boolean result = paymentService.processPayment(loanPayment);

        // Verify
        assertTrue(result);
        assertEquals("COMPLETED", loanPayment.getStatus());
        assertNotNull(loanPayment.getProcessedAt());
    }

    @Test
    void testProcessPaymentInvalidAccount() {
        loanPayment.setAccountNumber("INVALID");
        
        boolean result = paymentService.processPayment(loanPayment);
        
        assertFalse(result);
        assertEquals("FAILED", loanPayment.getStatus());
    }

    @Test
    void testGetPaymentStatistics() {
        // Initialize account
        paymentService.initializeAccountBalance("12345678", new BigDecimal("10000.00"));
        
        // Process payment
        paymentService.processPayment(loanPayment);

        // Get statistics
        var stats = paymentService.getPaymentStatistics();

        assertNotNull(stats);
        assertTrue((Long) stats.get("totalPayments") > 0);
    }

    @Test
    void testInitializeAccountBalance() {
        paymentService.initializeAccountBalance("87654321", new BigDecimal("5000.00"));
        
        Long balance = paymentService.getAccountBalance("87654321");
        
        assertEquals(5000L, balance);
    }
}
