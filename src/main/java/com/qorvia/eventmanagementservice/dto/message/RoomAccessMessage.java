package com.qorvia.eventmanagementservice.dto.message;

import lombok.Data;

@Data
public class RoomAccessMessage {
    private String type = "room-access-message";
    private String eventId;
    private Long userId;
    private String userEmail;
}
