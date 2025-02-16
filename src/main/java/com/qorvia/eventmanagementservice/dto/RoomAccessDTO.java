package com.qorvia.eventmanagementservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class RoomAccessDTO {
    private String eventId;
    private Long userId;
    private String userEmail;
}
