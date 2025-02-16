package com.qorvia.eventmanagementservice.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Location {
    private String lan;
    private String lon;
}
