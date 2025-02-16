package com.qorvia.eventmanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventBookingDTO {

    private String userName;
    private String email;
    private String address;
    private String country;
    private String state;
    private String city;
    private String zipCode;
    private String eventId;
    private List<TicketOptionDTO> ticketOptions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketOptionDTO {
        private String name;
        private int quantity;
        private int availableTickets;
        private double price;
        private double discountPrice;
    }
}
