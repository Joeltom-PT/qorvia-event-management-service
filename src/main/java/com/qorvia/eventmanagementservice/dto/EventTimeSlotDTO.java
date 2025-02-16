package com.qorvia.eventmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTimeSlotDTO {
    private String id;
    private String date;
    private String startTime;
    private String endTime;
    private String duration;
}
