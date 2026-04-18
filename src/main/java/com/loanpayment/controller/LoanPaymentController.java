package com.loanpayment.controller;

import com.loanpayment.model.LoanPayment;
import com.loanpayment.service.LoanPaymentProducerService;
import com.loanpayment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/loan-payments")
public class LoanPaymentController {

    @Autowired
    private LoanPaymentProducerService producerService;

    @Autowired
    private PaymentService paymentService;

    /**
     * Submit a single loan payment
     * POST /api/loan-payments/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitLoanPayment(@RequestBody LoanPayment loanPayment) {
        try {
            log.info("Received loan payment request for account: {}", loanPayment.getAccountNumber());

            if (loanPayment.getAccountNumber() == null || loanPayment.getAccountNumber().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Account number is required"));
            }

            if (loanPayment.getDueAmount() == null || loanPayment.getDueAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Due amount must be greater than zero"));
            }

            producerService.sendLoanPayment(loanPayment);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Loan payment submitted successfully");
            response.put("paymentId", loanPayment.getPaymentId());
            response.put("accountNumber", loanPayment.getAccountNumber());
            response.put("amount", loanPayment.getDueAmount());
            response.put("status", "PENDING");

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Error submitting loan payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Submit batch loan payments
     * POST /api/loan-payments/submit-batch
     */
    @PostMapping("/submit-batch")
    public ResponseEntity<?> submitBatchLoanPayments(@RequestBody List<LoanPayment> loanPayments) {
        try {
            if (loanPayments == null || loanPayments.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Loan payments list cannot be empty"));
            }

            log.info("Received batch loan payment request with {} payments", loanPayments.size());
            producerService.sendBatchLoanPayments(loanPayments);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Batch loan payments submitted successfully");
            response.put("count", loanPayments.size());
            response.put("status", "PENDING");

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Error submitting batch loan payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get payment record by payment ID
     * GET /api/loan-payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentRecord(@PathVariable String paymentId) {
        try {
            LoanPayment payment = paymentService.getPaymentRecord(paymentId);

            if (payment == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(payment);

        } catch (Exception e) {
            log.error("Error retrieving payment record", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all payment records
     * GET /api/loan-payments/records/all
     */
    @GetMapping("/records/all")
    public ResponseEntity<?> getAllPaymentRecords() {
        try {
            return ResponseEntity.ok(paymentService.getAllPaymentRecords());
        } catch (Exception e) {
            log.error("Error retrieving payment records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get account balance
     * GET /api/loan-payments/account/{accountNumber}/balance
     */
    @GetMapping("/account/{accountNumber}/balance")
    public ResponseEntity<?> getAccountBalance(@PathVariable String accountNumber) {
        try {
            Long balance = paymentService.getAccountBalance(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("accountNumber", accountNumber);
            response.put("balance", balance);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving account balance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Initialize account balance
     * POST /api/loan-payments/account/{accountNumber}/balance
     */
    @PostMapping("/account/{accountNumber}/balance")
    public ResponseEntity<?> initializeAccountBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal balance) {
        try {
            paymentService.initializeAccountBalance(accountNumber, balance);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account balance initialized successfully");
            response.put("accountNumber", accountNumber);
            response.put("balance", balance);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error initializing account balance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get payment statistics
     * GET /api/loan-payments/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getPaymentStatistics() {
        try {
            return ResponseEntity.ok(paymentService.getPaymentStatistics());
        } catch (Exception e) {
            log.error("Error retrieving payment statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * GET /api/loan-payments/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Loan Payment Kafka Application");
        response.put("topic", producerService.getTopicName());

        return ResponseEntity.ok(response);
    }
}
