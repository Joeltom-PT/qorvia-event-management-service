package com.qorvia.eventmanagementservice.dto;

import lombok.Builder;
import lombok.Data;

import lombok.Data;

@Data
@Builder
public class RefundDTO {
    private String sessionId;
    private long refundAmount;
    private String refundStatus;
    private String errorMessage;
}
