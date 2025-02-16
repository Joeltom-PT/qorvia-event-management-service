package com.qorvia.eventmanagementservice.dto.message.response;

import lombok.Data;

@Data
public class ValidateEventMessageResponse {
    private Boolean isValid;
    private String message;
}