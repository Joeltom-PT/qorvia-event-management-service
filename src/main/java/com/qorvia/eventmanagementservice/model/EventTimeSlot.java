package com.qorvia.eventmanagementservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTimeSlot {
    @Id
    private UUID id;
    private UUID eventId;
    private String date;
    private String startTime;
    private String endTime;
    private String duration;
}
