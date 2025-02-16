package com.qorvia.eventmanagementservice.dto.message;

import lombok.Data;

@Data
public class BookingCompletedNotificationMessage {
    private String type = "booking-completed-notification";
    private String userName;
    private String email;
    private Double totalAmount;
    private Double totalDiscount;
    private String paymentStatus;
    private String eventName;
    private String imageUrl;
}
