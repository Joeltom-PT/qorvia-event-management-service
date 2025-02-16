package com.qorvia.eventmanagementservice.repository;

import com.qorvia.eventmanagementservice.model.Booking;
import com.qorvia.eventmanagementservice.model.BookingStatus;
import com.qorvia.eventmanagementservice.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends MongoRepository<Booking, UUID> {

    Optional<Booking> findByPaymentSessionId(String paymentSessionId);

    List<Booking> findAllByEventId(String eventId);

    Optional<Booking> findByTempSessionId(String tempSessionId);

    Page<Booking> findByEventIdAndPaymentStatusIn(String eventId, List<PaymentStatus> paymentStatuses, Pageable pageable);

    List<Booking> findByUserIdAndPaymentStatusIn(Long userId, List<PaymentStatus> paymentStatus);

    List<Booking> findByUserIdAndBookingStatus(long userId, BookingStatus bookingStatus);

    List<Booking> findAllByEventIdAndUserId(String eventId, Long userId);

    List<Booking> findByEventId(String eventId);
}
