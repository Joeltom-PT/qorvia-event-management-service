package com.qorvia.eventmanagementservice.dto.client.event;

import com.qorvia.eventmanagementservice.model.PaymentStatus;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
public class PaymentStatusChangeEvent {
    public PaymentStatusChangeEvent(UUID bookingId, PaymentStatus paymentStatus) {
        this.bookingId = String.valueOf(bookingId);
        this.paymentStatus = paymentStatus;
    }

    private String bookingId;
    private PaymentStatus paymentStatus;
}
