package com.qorvia.eventmanagementservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveEventCategoryDTO{
    private UUID id;
    private String name;
}
