package com.qorvia.eventmanagementservice.dto.message.response;

import lombok.Data;

@Data
public class PaymentInfoMessageResponse {
    private String paymentUrl;
    private String sessionId;
    private String tempSessionId;
}
