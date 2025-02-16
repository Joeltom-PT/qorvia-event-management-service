package com.qorvia.eventmanagementservice.controller;

import com.qorvia.eventmanagementservice.dto.EventBookingDTO;
import com.qorvia.eventmanagementservice.dto.PaymentStatusChangeDTO;
import com.qorvia.eventmanagementservice.security.RequireRole;
import com.qorvia.eventmanagementservice.security.RequireRoles;
import com.qorvia.eventmanagementservice.security.Roles;
import com.qorvia.eventmanagementservice.service.BookingService;
import com.qorvia.eventmanagementservice.service.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final JwtService jwtService;

    @PostMapping
    @RequireRole(role = Roles.USER)
    public ResponseEntity<?> processBooking(@RequestBody EventBookingDTO eventBookingDTO, HttpServletRequest servletRequest){
        String token = jwtService.getJWTFromRequest(servletRequest);
        Long userId = jwtService.getUserIdFromToken(token);
        String email = jwtService.getEmailFromToken(token);
        log.info("booking request from user : {} for the event : {}", userId, eventBookingDTO.getEventId());
        return bookingService.processBooking(userId, email, eventBookingDTO);
    }


    @GetMapping("/info/{id}")
    @RequireRole(role = Roles.USER)
    public ResponseEntity<?> getBookingByUser(
            @RequestParam("isFree") boolean isFree,
            @PathVariable("id") String id,
            HttpServletRequest servletRequest) {

        Long userId = jwtService.getUserIdFormRequest(servletRequest);
        log.info("Getting booking by user with data free: {}, id: {}", isFree, id);

        if (isFree) {
            return ResponseEntity.ok(bookingService.getBookingInfoById(id));
        } else {
            log.info("trying to fetch booking info using temp session Id : {}", id);
            return ResponseEntity.ok(bookingService.getBookingInfoBySessionId(id));
        }
    }



    @GetMapping("/booked-users/{id}")
    @RequireRole(role = Roles.ORGANIZER)
    public ResponseEntity<?> getEventBookedUsersByOrganizer(
            @PathVariable("id") String eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest servletRequest) {

        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);

        return bookingService.getEventBookedUsersByOrganizer(eventId, organizerId, page, size);
    }


    @GetMapping("/get-all/byUser")
    @RequireRole(role = Roles.USER)
    public ResponseEntity<?> getAllBookingsByUser(HttpServletRequest servletRequest){
        Long userId = jwtService.getUserIdFormRequest(servletRequest);
        return bookingService.getAllBookingsByUser(userId);
    }


    @PostMapping("/payment-status-update")
    public ResponseEntity<String> paymentStatusUpdate(@RequestBody PaymentStatusChangeDTO paymentStatusChangeDTO){
        bookingService.paymentStatusChangeHandle(paymentStatusChangeDTO);
        return ResponseEntity.ok("Payment status updated successful!");
    }

    @PostMapping("/cancel/{id}")
    @RequireRole(role = Roles.USER)
    public ResponseEntity<?> cancelBooking(@PathVariable("id") String bookingId, HttpServletRequest servletRequest){
      Long userId = jwtService.getUserIdFormRequest(servletRequest);
      return bookingService.cancelBooking(bookingId, userId);
    }


}

