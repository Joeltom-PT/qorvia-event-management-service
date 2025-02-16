package com.qorvia.eventmanagementservice.dto.response;

import com.qorvia.eventmanagementservice.model.EventType;
import lombok.Data;

@Data
public class GetOnlineEventDetailResponse {
    private String id;

    private String name;

    private String categoryId;

    private EventType eventType;

    private String description;

    private String imageUrl;
}
