package com.qorvia.eventmanagementservice.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineEventTickets {
    private Integer totalTickets;
    private List<TicketCategory> categories;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketCategory {
        private String name;
        private Integer totalTickets;
        private Double price;
        private String discountType;
        private Double discountValue;
        private String description;
    }
}
