package com.qorvia.eventmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qorvia.eventmanagementservice.model.Booking;
import com.qorvia.eventmanagementservice.model.BookingStatus;
import com.qorvia.eventmanagementservice.model.PaymentStatus;
import lombok.Data;

@Data
public class UserBookingInfo {
    private String bookingId;
    private Long userId;
    private String eventId;
    @JsonProperty("isOnline")
    private boolean isOnline;
    private String eventName;
    private String imageUrl;
    private Double totalAmount;
    private Double totalDiscount;
    private String startTimeAndDate;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private Booking.Ticket ticket;
}


