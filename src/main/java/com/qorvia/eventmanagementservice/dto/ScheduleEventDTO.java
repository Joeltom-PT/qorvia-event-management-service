package com.qorvia.eventmanagementservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScheduleEventDTO {
    private String eventId;
    private String name;
    private Long organizerId;
    private String imageUrl;
    private String startDateAndTime;
    private String endDateAndTime;
}
