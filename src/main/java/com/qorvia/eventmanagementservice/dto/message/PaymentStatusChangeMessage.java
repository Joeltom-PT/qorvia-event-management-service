package com.qorvia.eventmanagementservice.dto.message;

import com.qorvia.eventmanagementservice.model.PaymentStatus;
import lombok.Data;

@Data
public class PaymentStatusChangeMessage {
    private String type = "payment-status-change";
    private String paymentSessionId;
    private String userEmail;
    private String eventId;
    private PaymentStatus paymentStatus;
}
