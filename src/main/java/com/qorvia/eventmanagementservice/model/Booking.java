package com.qorvia.eventmanagementservice.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking {
    @Id
    private UUID id;
    private Long userId;
    private String eventId;
    private String paymentSessionId;
    private String tempSessionId;
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
    private List<Ticket> tickets;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    @CreatedDate
    private String createdAt;
    @LastModifiedDate
    private String updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ticket {
        private String name;
        private int quantity;
        private int ticketsInCategory;
        private double price;
        private double discountPrice;
    }
}
