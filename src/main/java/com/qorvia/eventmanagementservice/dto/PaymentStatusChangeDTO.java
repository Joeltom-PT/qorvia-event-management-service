package com.qorvia.eventmanagementservice.dto;

import com.qorvia.eventmanagementservice.model.PaymentStatus;
import lombok.Data;

@Data
public class PaymentStatusChangeDTO {
    private String paymentSessionId;
    private String userEmail;
    private String eventId;
    private PaymentStatus paymentStatus;
}
