package com.qorvia.eventmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnlineEventTicketDTO {
    private String totalTickets;
    @JsonProperty("isFreeEvent")
    private boolean isFreeEvent;
    private String price;
    private boolean hasEarlyBirdDiscount;
    private String discountType;
    private String discountValue;
}
