package com.qorvia.eventmanagementservice.dto;

import com.qorvia.eventmanagementservice.dto.request.CreateOfflineEventTicketRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfflineEventTicketsDTO {
    private Integer totalTickets;
    private List<CreateOfflineEventTicketRequest.TicketCategory> categories;

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
