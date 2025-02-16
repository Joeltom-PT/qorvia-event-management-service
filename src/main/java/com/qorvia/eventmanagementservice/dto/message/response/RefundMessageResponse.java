package com.qorvia.eventmanagementservice.dto.message.response;

import lombok.Data;

@Data
public class RefundMessageResponse {
    private String sessionId;
    private long refundAmount;
    private String refundStatus;
    private String errorMessage;
}
