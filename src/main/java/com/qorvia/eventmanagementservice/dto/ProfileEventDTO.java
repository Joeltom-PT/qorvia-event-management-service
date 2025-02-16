package com.qorvia.eventmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qorvia.eventmanagementservice.model.EventState;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProfileEventDTO {
        private UUID eventId;
        private Long organizerId;
        private String eventName;
        private String imgUrl;
        private String eventDescription;
        @JsonProperty("isOnline")
        private boolean isOnline;
        private String eventCategory;
        private String startDateAndTime;
}
