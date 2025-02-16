package com.qorvia.eventmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qorvia.eventmanagementservice.model.EventState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInfoDTO {
    private UUID eventId;
    private Long organizerId;
    private String eventName;
    private String imageUrl;
    @JsonProperty("isOnline")
    private boolean isOnline;
    private String eventCategory;
    private String startDateAndTime;
    private String endDateAndTime;
}