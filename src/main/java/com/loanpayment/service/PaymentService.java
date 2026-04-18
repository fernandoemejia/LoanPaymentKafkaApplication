package com.loanpayment.service;

import com.loanpayment.model.LoanPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PaymentService {

    // In-memory store for payment records (in production, use database)
    private final Map<String, LoanPayment> paymentRecords = new ConcurrentHashMap<>();
    private final Map<String, Long> accountBalance = new ConcurrentHashMap<>();

    /**
     * Initialize account balance (mock data)
     */
    public void initializeAccountBalance(String accountNumber, BigDecimal balance) {
        accountBalance.put(accountNumber, balance.longValue());
        log.info("Initialized account {} with balance: {}", accountNumber, balance);
    }

    /**
     * Process loan payment
     */
    public boolean processPayment(LoanPayment loanPayment) {
        try {
            String accountNumber = loanPayment.getAccountNumber();
            BigDecimal dueAmount = loanPayment.getDueAmount();

            log.info("Processing payment for account: {} with amount: {}", accountNumber, dueAmount);

            // Verify account exists (in production, check against database)
            if (!isValidAccount(accountNumber)) {
                log.warn("Invalid account number: {}", accountNumber);
                loanPayment.setStatus("FAILED");
                loanPayment.setProcessedAt(LocalDateTime.now());
                paymentRecords.put(loanPayment.getPaymentId(), loanPayment);
                return false;
            }

            // Process payment (in production, integrate with payment gateway)
            boolean paymentSuccess = executePayment(accountNumber, dueAmount);

            if (paymentSuccess) {
                loanPayment.setStatus("COMPLETED");
                loanPayment.setProcessedAt(LocalDateTime.now());
                paymentRecords.put(loanPayment.getPaymentId(), loanPayment);
                
                log.info("Payment successfully processed for account: {} with amount: {}", 
                         accountNumber, dueAmount);
                return true;
            } else {
                loanPayment.setStatus("FAILED");
                loanPayment.setProcessedAt(LocalDateTime.now());
                paymentRecords.put(loanPayment.getPaymentId(), loanPayment);
                
                log.warn("Payment processing failed for account: {}", accountNumber);
                return false;
            }

        } catch (Exception e) {
            log.error("Error processing payment", e);
            loanPayment.setStatus("ERROR");
            loanPayment.setProcessedAt(LocalDateTime.now());
            paymentRecords.put(loanPayment.getPaymentId(), loanPayment);
            return false;
        }
    }

    /**
     * Validate account exists
     */
    private boolean isValidAccount(String accountNumber) {
        // In production, check against database
        return accountNumber != null && !accountNumber.isEmpty() && accountNumber.matches("^[0-9]{8,12}$");
    }

    /**
     * Execute payment (mock implementation)
     */
    private boolean executePayment(String accountNumber, BigDecimal dueAmount) {
        // Simulate payment processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mock: 95% success rate
        boolean success = Math.random() < 0.95;
        
        if (success) {
            // Deduct amount from account balance (mock)
            accountBalance.computeIfPresent(accountNumber, (key, balance) -> 
                balance - dueAmount.longValue());
        }

        return success;
    }

    /**
     * Get payment record by payment ID
     */
    public LoanPayment getPaymentRecord(String paymentId) {
        return paymentRecords.get(paymentId);
    }

    /**
     * Get all payment records
     */
    public Map<String, LoanPayment> getAllPaymentRecords() {
        return new HashMap<>(paymentRecords);
    }

    /**
     * Get account balance
     */
    public Long getAccountBalance(String accountNumber) {
        return accountBalance.getOrDefault(accountNumber, 0L);
    }

    /**
     * Get payment statistics
     */
    public Map<String, Object> getPaymentStatistics() {
        long totalPayments = paymentRecords.size();
        long completedPayments = paymentRecords.values().stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .count();
        long failedPayments = paymentRecords.values().stream()
                .filter(p -> "FAILED".equals(p.getStatus()))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPayments", totalPayments);
        stats.put("completedPayments", completedPayments);
        stats.put("failedPayments", failedPayments);
        stats.put("successRate", totalPayments > 0 ? 
                  (double) completedPayments / totalPayments * 100 : 0);

        return stats;
    }
}
