package com.qorvia.eventmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RegisteredEventDTO {

    private String id;
    private String name;
    @JsonProperty("isOnline")
    private boolean isOnline;
    @JsonProperty("isLive")
    private boolean isLive;
    @JsonProperty("isCompleted")
    private boolean isCompleted;
    private String startDateAndTime;
    private String endDateAndTime;
    private String imageUrl;
}
