package com.qorvia.eventmanagementservice.dto;

import lombok.Data;

@Data
public class BookingCompletedNotificationDTO {
    private String userName;
    private String email;
    private Double totalAmount;
    private Double totalDiscount;
    private String paymentStatus;
    private String eventName;
    private String imageUrl;
}
