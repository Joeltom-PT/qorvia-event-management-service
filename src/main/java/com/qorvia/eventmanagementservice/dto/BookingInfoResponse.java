package com.qorvia.eventmanagementservice.dto;

import com.qorvia.eventmanagementservice.model.Booking;
import com.qorvia.eventmanagementservice.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfoResponse {
    private String id;
    private Long userId;
    private String eventId;
    private String paymentSessionId;
    private String userName;
    private String email;
    private String address;
    private String country;
    private String state;
    private String city;
    private String zipCode;
    private Double totalAmount;
    private Double totalDiscount;
    private boolean isOnline;
    private List<BookingTicketDTO> tickets;
    private EventInfoDTO eventInfo;
    private PaymentStatus paymentStatus;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingTicketDTO {
        private String name;
        private int quantity;
        private int ticketsInCategory;
        private double price;
        private double discountPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventInfoDTO {
        private String id;
        private String name;
        private String imageUrl;
    }
}
