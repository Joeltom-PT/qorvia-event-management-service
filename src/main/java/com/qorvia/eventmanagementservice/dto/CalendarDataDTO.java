package com.qorvia.eventmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarDataDTO {
    private String eventId;
    private Long organizerId;
    private String name;
    private String startTimeAndDate;
    private String endTimeAndDate;
    private String imageUrl;
    @JsonProperty("isOnline")
    private boolean isOnline;
}
