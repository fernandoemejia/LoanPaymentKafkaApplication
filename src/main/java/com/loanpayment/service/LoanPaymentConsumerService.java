package com.loanpayment.service;

import com.loanpayment.model.LoanPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoanPaymentConsumerService {

    @Autowired
    private PaymentService paymentService;

    /**
     * Listen to loan payments topic and process payments
     */
    @KafkaListener(
            topics = "${kafka.topic.loan-payments:loan-payments-topic}",
            groupId = "${spring.kafka.consumer.group-id:loan-payment-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLoanPayment(@Payload LoanPayment loanPayment) {
        try {
            log.info("Received loan payment message - Account: {}, Amount: {}, PaymentId: {}", 
                     loanPayment.getAccountNumber(), loanPayment.getDueAmount(), loanPayment.getPaymentId());

            // Process the payment
            boolean paymentProcessed = paymentService.processPayment(loanPayment);

            if (paymentProcessed) {
                log.info("Payment processed successfully - PaymentId: {}", loanPayment.getPaymentId());
            } else {
                log.warn("Payment processing failed - PaymentId: {}", loanPayment.getPaymentId());
            }

        } catch (Exception e) {
            log.error("Error consuming loan payment message", e);
            throw new RuntimeException("Error processing loan payment", e);
        }
    }

    /**
     * Alternative listener for batch processing
     */
    @KafkaListener(
            topics = "${kafka.topic.loan-payments:loan-payments-topic}",
            groupId = "${spring.kafka.consumer.group-id:loan-payment-group}-batch",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLoanPaymentBatch(@Payload LoanPayment loanPayment) {
        log.debug("Processing loan payment in batch mode - Account: {}", loanPayment.getAccountNumber());
        consumeLoanPayment(loanPayment);
    }
}
