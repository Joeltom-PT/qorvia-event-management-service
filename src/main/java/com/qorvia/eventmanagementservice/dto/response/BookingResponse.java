package com.qorvia.eventmanagementservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BookingResponse {
    private String eventId;
    @JsonProperty("isFree")
    private boolean isFree;
    private String paymentLink;
    private String bookingId;
}
