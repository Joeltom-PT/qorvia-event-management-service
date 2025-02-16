package com.qorvia.eventmanagementservice.dto;

import com.qorvia.eventmanagementservice.model.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCategoryDTO {
    private UUID id;

    private String name;

    private String description;

    private CategoryStatus status;

}
