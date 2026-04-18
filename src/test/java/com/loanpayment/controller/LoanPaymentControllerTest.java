package com.loanpayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanpayment.model.LoanPayment;
import com.loanpayment.service.LoanPaymentProducerService;
import com.loanpayment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoanPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanPaymentProducerService producerService;

    @MockBean
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
    void testSubmitLoanPaymentSuccess() throws Exception {
        mockMvc.perform(post("/api/loan-payments/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanPayment)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Loan payment submitted successfully"))
                .andExpect(jsonPath("$.accountNumber").value("12345678"));
    }

    @Test
    void testSubmitLoanPaymentMissingAccountNumber() throws Exception {
        loanPayment.setAccountNumber(null);
        
        mockMvc.perform(post("/api/loan-payments/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanPayment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testSubmitLoanPaymentInvalidAmount() throws Exception {
        loanPayment.setDueAmount(BigDecimal.ZERO);
        
        mockMvc.perform(post("/api/loan-payments/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanPayment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/loan-payments/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Loan Payment Kafka Application"));
    }
}
