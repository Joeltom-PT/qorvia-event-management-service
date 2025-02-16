package com.qorvia.eventmanagementservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOfflineEventTicketRequest {
    private Integer totalTickets;
    private List<TicketCategory> categories;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketCategory {
        private String name;
        private String totalTickets;
        private String price;
        private String discountType;
        private String discountValue;
        private String description;
    }
}