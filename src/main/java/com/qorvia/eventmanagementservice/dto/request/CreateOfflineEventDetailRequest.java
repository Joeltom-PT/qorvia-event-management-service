package com.qorvia.eventmanagementservice.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CreateOfflineEventDetailRequest {
    private String id;
    private String name;
    private String categoryId;
    private String description;
    private String imageUrl;
    private Double lat;
    private Double lng;
    private String address;
}
