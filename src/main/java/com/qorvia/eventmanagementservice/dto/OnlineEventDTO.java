package com.qorvia.eventmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qorvia.eventmanagementservice.model.EventTimeSlot;
import com.qorvia.eventmanagementservice.model.OnlineEventTicket;
import com.qorvia.eventmanagementservice.model.OnlineEventSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnlineEventDTO {

    private String name;
    private String description;
    private String categoryId;
    private String categoryName;
    @JsonProperty("isOnline")
    private boolean isOnline;
    private String imageUrl;
    private Long organizerId;
    @JsonProperty("isRegistered")
    private boolean isRegistered;
    private OnlineEventTicket onlineEventTicket;
    private List<EventTimeSlot> timeSlots;

    private OnlineEventSetting onlineEventSetting;
}
