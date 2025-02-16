package com.qorvia.eventmanagementservice.dto.message;

import com.qorvia.eventmanagementservice.dto.PaymentRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PaymentRequestMessage {
    private String type = "payment-request-message";
    private String currency;
    private int amount;
    private String email;
    private String eventId;
    private Long eventOrganizerId;
    private String eventName;
    private String imgUrl;

}
