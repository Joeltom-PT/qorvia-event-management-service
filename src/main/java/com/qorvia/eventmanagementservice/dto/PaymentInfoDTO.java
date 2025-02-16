package com.qorvia.eventmanagementservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentInfoDTO {
    private String paymentUrl;
    private String sessionId;
    private String tempSessionId;
}
