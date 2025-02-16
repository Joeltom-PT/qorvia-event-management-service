package com.qorvia.eventmanagementservice.dto.request;

import lombok.Data;

@Data
public class CreateOnlineEventDetailRequest {
    private String id;
    private String organizerId;
    private String name;
    private String categoryId;
    private String typeName;
    private String description;
    private String imageUrl;
}
