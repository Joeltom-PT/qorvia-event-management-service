package com.qorvia.eventmanagementservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineEventTicket {
    private Integer totalTickets;
    private boolean isFreeEvent;
    private Integer price;
    private boolean hasEarlyBirdDiscount;
    private String discountType;
    private Integer discountValue;
}
