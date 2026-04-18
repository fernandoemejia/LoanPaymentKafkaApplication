package com.loanpayment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanPayment implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("dueAmount")
    private BigDecimal dueAmount;

    @JsonProperty("paymentDate")
    private LocalDateTime paymentDate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("paymentId")
    private String paymentId;

    @JsonProperty("processedAt")
    private LocalDateTime processedAt;
}
