package com.qorvia.eventmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qorvia.eventmanagementservice.model.EventTimeSlot;
import com.qorvia.eventmanagementservice.model.OfflineEventTickets;
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
public class OfflineEventDTO {
    private String eventId;
    private String name;
    private String description;
    private String categoryId;
    private String categoryName;
    @JsonProperty("isOnline")
    private boolean isOnline;
    @JsonProperty("isBooked")
    private boolean isBooked;
    private String imageUrl;
    private String address;
    private Double lan;
    private Double lon;
    private Long organizerId;
    private OfflineEventTickets offlineEventTickets;
    private List<EventTimeSlot> timeSlots;

    private OnlineEventSetting onlineEventSetting;
}
