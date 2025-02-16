package com.qorvia.eventmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LiveEvent {
    private String id;
    private String name;
    private String startDateAndTime;
    private String endDateAndTime;
    private String imageUrl;
    @JsonProperty("isOnline")
    private boolean isOnline;
}
