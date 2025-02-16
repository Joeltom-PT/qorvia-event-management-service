package com.qorvia.eventmanagementservice.service;

import com.qorvia.eventmanagementservice.model.Event;
import com.qorvia.eventmanagementservice.model.Booking;
import com.qorvia.eventmanagementservice.repository.BookingRepository;
import com.qorvia.eventmanagementservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartServiceImpl implements ChartService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ResponseEntity<?> getChartDataByBookings(
            Long organizerId,
            String eventId, String type) {
        Optional<Event> optionalEvent = eventRepository.findById(UUID.fromString(eventId));
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }
        Event event = optionalEvent.get();

        if (event.isDeleted()) {
            return ResponseEntity.status(HttpStatus.GONE).body("The event has been deleted");
        }

        List<Booking> bookings = bookingRepository.findByEventId(eventId);

        Map<String, Integer> chartData;
        switch (type.toLowerCase()) {
            case "daily":
                chartData = groupBookingsByDaily(bookings);
                break;
            case "weekly":
                chartData = groupBookingsByWeekly(bookings);
                break;
            case "monthly":
                chartData = groupBookingsByMonthly(bookings);
                break;
            case "yearly":
                chartData = groupBookingsByYearly(bookings);
                break;
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid type. Supported types: daily, weekly, monthly, yearly");
        }

        return ResponseEntity.ok(chartData);
    }

    private LocalDateTime parseCreatedAt(String createdAt) {
        try {
            return LocalDateTime.parse(createdAt, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format for createdAt: " + createdAt);
        }
    }

    private Map<String, Integer> groupBookingsByDaily(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> parseCreatedAt(booking.getCreatedAt())
                                .toLocalDate()
                                .toString(),
                        Collectors.summingInt(b -> 1)
                ));
    }

    private Map<String, Integer> groupBookingsByWeekly(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> parseCreatedAt(booking.getCreatedAt())
                                .toLocalDate()
                                .with(DayOfWeek.MONDAY)
                                .toString(),
                        Collectors.summingInt(b -> 1)
                ));
    }

    private Map<String, Integer> groupBookingsByMonthly(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> parseCreatedAt(booking.getCreatedAt())
                                .format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.summingInt(b -> 1)
                ));
    }

    private Map<String, Integer> groupBookingsByYearly(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> parseCreatedAt(booking.getCreatedAt())
                                .format(DateTimeFormatter.ofPattern("yyyy")),
                        Collectors.summingInt(b -> 1)
                ));
    }
}
