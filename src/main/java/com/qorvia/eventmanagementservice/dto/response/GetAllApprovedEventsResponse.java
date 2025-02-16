package com.qorvia.eventmanagementservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qorvia.eventmanagementservice.model.EventState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllApprovedEventsResponse {
    private List<EventInfo> events;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventInfo {
        private UUID eventId;
        private String eventName;
        private EventState eventState;
        @JsonProperty("isOnline")
        private boolean isOnline;
        private String eventCategory;
        private String startDateAndTime;
        private String endDateAndTime;
    }
}
