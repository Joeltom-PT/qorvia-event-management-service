package com.qorvia.eventmanagementservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "event_categories")
public class EventCategory {

    @Id
    private UUID id;

    private String name;

    private String description;

    private CategoryStatus status;
}
