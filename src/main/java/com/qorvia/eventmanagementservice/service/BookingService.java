package com.qorvia.eventmanagementservice.service;

import com.qorvia.eventmanagementservice.dto.BookingInfoResponse;
import com.qorvia.eventmanagementservice.dto.EventBookingDTO;
import com.qorvia.eventmanagementservice.dto.PaymentStatusChangeDTO;
import org.springframework.http.ResponseEntity;

public interface BookingService {

    ResponseEntity<?> processBooking(Long userId, String email, EventBookingDTO eventBookingDTO);

    void paymentStatusChangeHandle(PaymentStatusChangeDTO paymentStatusChangeDTO);

    BookingInfoResponse getBookingInfoById(String id);
    BookingInfoResponse getBookingInfoBySessionId(String sessionId);

    ResponseEntity<?> getEventBookedUsersByOrganizer(String eventId, Long organizerId, int page, int size);

    ResponseEntity<?> getAllBookingsByUser(Long userId);

    ResponseEntity<?> cancelBooking(String bookingId, Long userId);
}
