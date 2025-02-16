package com.qorvia.eventmanagementservice.dto.message;

import lombok.Data;

@Data
public class RefundRequestMessage {
    private String type = "refund-request";
    private String sessionId;
    private int refundPercentage;
}
