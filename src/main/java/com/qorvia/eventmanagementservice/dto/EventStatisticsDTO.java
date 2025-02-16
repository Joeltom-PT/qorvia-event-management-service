package com.qorvia.eventmanagementservice.dto;

import lombok.Data;

@Data
public class EventStatisticsDTO {
    private int totalEvents;
    private int todayEvents;
    private int thisWeekEvents;
}
