package com.qorvia.eventmanagementservice.dto.message;

import lombok.Data;

@Data
public class ScheduleEventMessage {
    private String type;
    private String eventId;
    private String name;
    private Long organizerId;
    private String imageUrl;
    private String startDateAndTime;
    private String endDateAndTime;
}
