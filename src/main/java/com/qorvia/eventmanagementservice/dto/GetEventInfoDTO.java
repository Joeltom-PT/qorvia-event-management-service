package com.qorvia.eventmanagementservice.dto;

import lombok.Data;

@Data
public class GetEventInfoDTO {
    private String eventId;
    private Long organizerId;
    private String imageUrl;
    private String category;
    private String title;
    private String date;
}
