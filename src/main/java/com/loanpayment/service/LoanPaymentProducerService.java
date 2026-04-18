package com.loanpayment.service;

import com.loanpayment.model.LoanPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class LoanPaymentProducerService {

    @Autowired
    private KafkaTemplate<String, LoanPayment> kafkaTemplate;

    @Value("${kafka.topic.loan-payments:loan-payments-topic}")
    private String loanPaymentsTopic;

    /**
     * Send loan payment details to Kafka topic
     */
    public void sendLoanPayment(LoanPayment loanPayment) {
        try {
            // Set payment ID if not already set
            if (loanPayment.getPaymentId() == null) {
                loanPayment.setPaymentId(UUID.randomUUID().toString());
            }

            // Set status to PENDING
            loanPayment.setStatus("PENDING");
            loanPayment.setPaymentDate(LocalDateTime.now());

            Message<LoanPayment> message = MessageBuilder
                    .withPayload(loanPayment)
                    .setHeader(KafkaHeaders.TOPIC, loanPaymentsTopic)
                    .setHeader("kafka_messageKey", loanPayment.getAccountNumber())
                    .build();

            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent loan payment for account: {} with amount: {}", 
                             loanPayment.getAccountNumber(), loanPayment.getDueAmount());
                } else {
                    log.error("Failed to send loan payment for account: {}", 
                              loanPayment.getAccountNumber(), ex);
                }
            });

        } catch (Exception e) {
            log.error("Error sending loan payment for account: {}", 
                      loanPayment.getAccountNumber(), e);
            throw new RuntimeException("Failed to send loan payment", e);
        }
    }

    /**
     * Send multiple loan payments in batch
     */
    public void sendBatchLoanPayments(java.util.List<LoanPayment> loanPayments) {
        log.info("Sending batch of {} loan payments", loanPayments.size());
        loanPayments.forEach(this::sendLoanPayment);
    }

    /**
     * Get the topic name
     */
    public String getTopicName() {
        return loanPaymentsTopic;
    }
}
