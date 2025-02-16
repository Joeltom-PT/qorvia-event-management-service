package com.qorvia.eventmanagementservice.service;

import com.qorvia.eventmanagementservice.clients.CommunicationServiceClient;
import com.qorvia.eventmanagementservice.clients.NotificationServiceClient;
import com.qorvia.eventmanagementservice.clients.PaymentServiceClient;
import com.qorvia.eventmanagementservice.dto.*;
import com.qorvia.eventmanagementservice.dto.response.BookedUsersListResponse;
import com.qorvia.eventmanagementservice.dto.response.BookingResponse;
import com.qorvia.eventmanagementservice.model.*;
import com.qorvia.eventmanagementservice.repository.BookingRepository;
import com.qorvia.eventmanagementservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService{

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final PaymentServiceClient paymentClient;
    private final NotificationServiceClient notificationClient;
    private final ApplicationEventPublisher eventPublisher;
    private final CommunicationServiceClient communicationClient;

    @Override
    public ResponseEntity<?> processBooking(Long userId, String email, EventBookingDTO eventBookingDTO) {
        log.info("Processing booking for userId: {}", userId);
        log.info("EventBookingDTO: {}", eventBookingDTO);

        Optional<Event> eventOptional = eventRepository.findById(UUID.fromString(eventBookingDTO.getEventId()));
        if (eventOptional.isEmpty()) {
            log.error("Event not found for eventId: {}", eventBookingDTO.getEventId());
            return ResponseEntity.badRequest().body("Event not found, something went wrong!");
        }

        Event event = eventOptional.get();
        double totalAmount = 0;
        double totalDiscount = 0;

        log.info("Found event: {}", event);

        if (event.isOnline()) {
            OnlineEventTicket onlineTicket = event.getOnlineEventTicket();

            Integer price = onlineTicket.getPrice();
            Integer discountValue = onlineTicket.getDiscountValue();
            String discountType = onlineTicket.getDiscountType();

            if (price == null && onlineTicket.isFreeEvent()) {
                totalAmount = 0;
            } else if (price != null) {
                if ("percentage".equals(discountType)) {
                    totalAmount = price - ((double) (price * (discountValue != null ? discountValue : 0)) / 100);
                } else {
                    totalAmount = price - (discountValue != null ? discountValue : 0);
                }
            } else {
                throw new IllegalArgumentException("Price cannot be null for a non-free event");
            }


            log.info("Processing online event ticket: {}", onlineTicket);


            if (onlineTicket.getTotalTickets() < 1) {
                log.warn("No tickets available for online event!");
                return ResponseEntity.badRequest().body("No tickets available for online event!");
            }

            onlineTicket.setTotalTickets(onlineTicket.getTotalTickets() - 1);
        } else {
            int totalTicketsNeeded = 0;

            for (EventBookingDTO.TicketOptionDTO option : eventBookingDTO.getTicketOptions()) {
                log.info("Processing ticket option: {}", option);

                OfflineEventTickets offlineTickets = event.getOfflineEventTickets();
                OfflineEventTickets.TicketCategory category = offlineTickets.getCategories().stream()
                        .filter(cat -> cat.getName().equals(option.getName()))
                        .findFirst()
                        .orElse(null);

                if (category == null) {
                    log.error("Category not found for {}", option.getName());
                    return ResponseEntity.badRequest().body("Invalid ticket category.");
                }

                int quantity = option.getQuantity();

                totalTicketsNeeded += quantity;

                double pricePerTicket = category.getPrice();
                double discountedPricePerTicket = pricePerTicket;

                if (Objects.equals(category.getDiscountType(), "percentage")) {
                    discountedPricePerTicket = pricePerTicket - (pricePerTicket * category.getDiscountValue() / 100);
                } else if (Objects.equals(category.getDiscountType(), "fixed")) {
                    discountedPricePerTicket = pricePerTicket - category.getDiscountValue();
                }

                totalAmount += discountedPricePerTicket * quantity;

                double discountPerTicket = pricePerTicket - discountedPricePerTicket;
                double totalDiscountForCategory = discountPerTicket * quantity;
                totalDiscount += totalDiscountForCategory;
            }

            if (event.getOfflineEventTickets().getTotalTickets() - totalTicketsNeeded < 0) {
                log.warn("No tickets available for offline event!");
                return ResponseEntity.badRequest().body("No tickets available for offline event!");
            }

            event.getOfflineEventTickets().setTotalTickets(event.getOfflineEventTickets().getTotalTickets() - totalTicketsNeeded);
        }

        int finalAmount = (int) (totalAmount - totalDiscount);

        if (finalAmount <= 0){
            RoomAccessDTO roomAccessDTO = new RoomAccessDTO();
            roomAccessDTO.setUserId(userId);
            roomAccessDTO.setUserEmail(email);
            roomAccessDTO.setEventId(String.valueOf(event.getId()));
            communicationClient.allowRoom(roomAccessDTO);
            log.info("Allow access requested to communication service");
        }

        String paymentLink = null;
        String paymentSessionId = null;
        String tempSessionId = null;

        if (finalAmount >= 10){
            PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO();
            paymentRequestDTO.setCurrency("inr");
            paymentRequestDTO.setAmount(finalAmount);
            paymentRequestDTO.setEmail(email);

            PaymentRequestDTO.EventData eventData = new PaymentRequestDTO.EventData();
            eventData.setEventId(String.valueOf(event.getId()));
            eventData.setEventOrganizerId(event.getOrganizerId());
            eventData.setEventName(event.getName());
            eventData.setImgUrl(event.getImageUrl());

            paymentRequestDTO.setEventData(eventData);

            PaymentInfoDTO paymentInfoDTO  = paymentClient.getPaymentInfo(paymentRequestDTO);

            paymentLink = paymentInfoDTO.getPaymentUrl();
            paymentSessionId = paymentInfoDTO.getSessionId();
            tempSessionId = paymentInfoDTO.getTempSessionId();

            log.info("Payment Link: {}", paymentLink);
        }

        eventRepository.save(event);


        List<Booking.Ticket> tickets = eventBookingDTO.getTicketOptions().stream()
                .map(ticketOptionDTO -> Booking.Ticket.builder()
                        .name(ticketOptionDTO.getName())
                        .quantity(ticketOptionDTO.getQuantity())
                        .ticketsInCategory(ticketOptionDTO.getAvailableTickets())
                        .price(ticketOptionDTO.getPrice())
                        .discountPrice(ticketOptionDTO.getDiscountPrice())
                        .build())
                .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAt = LocalDateTime.now().format(formatter);

        Booking.BookingBuilder bookingBuilder = Booking.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .eventId(eventBookingDTO.getEventId())
                .isOnline(event.isOnline())
                .userName(eventBookingDTO.getUserName())
                .email(eventBookingDTO.getEmail())
                .address(eventBookingDTO.getAddress())
                .country(eventBookingDTO.getCountry())
                .state(eventBookingDTO.getState())
                .city(eventBookingDTO.getCity())
                .createdAt(createdAt)
                .zipCode(eventBookingDTO.getZipCode())
                .totalAmount((double) finalAmount)
                .totalDiscount(totalDiscount)
                .tickets(tickets);

        if (paymentLink == null) {
            bookingBuilder.paymentStatus(PaymentStatus.NONE);
            bookingBuilder.bookingStatus(BookingStatus.CONFIRMED);
        } else {
            bookingBuilder.paymentStatus(PaymentStatus.PENDING);
            bookingBuilder.bookingStatus(BookingStatus.PENDING);
            bookingBuilder.paymentSessionId(paymentSessionId);
            bookingBuilder.tempSessionId(tempSessionId);
        }

        Booking booking = bookingBuilder.build();

        bookingRepository.save(booking);

        BookingResponse bookingResponse = new BookingResponse();
        if (paymentLink != null){
            bookingResponse.setFree(false);
            bookingResponse.setPaymentLink(paymentLink);
        } else  {
            bookingResponse.setFree(true);
            bookingResponse.setPaymentLink(null);
        }

        bookingResponse.setEventId(String.valueOf(event.getId()));
        bookingResponse.setBookingId(String.valueOf(booking.getId()));

        return ResponseEntity.ok(bookingResponse);
    }


    @Override
    @Transactional
    public void paymentStatusChangeHandle(PaymentStatusChangeDTO paymentStatusChangeDTO) {
        Optional<Booking> optionalBooking = bookingRepository.findByPaymentSessionId(paymentStatusChangeDTO.getPaymentSessionId());

        if (optionalBooking.isEmpty()){
            throw new RuntimeException("Something went wrong");
        }

        Booking booking = optionalBooking.get();

        if (booking.getPaymentStatus() == PaymentStatus.PENDING && booking.getBookingStatus() == BookingStatus.PENDING) {
            if (paymentStatusChangeDTO.getPaymentStatus() == PaymentStatus.EXPIRED || paymentStatusChangeDTO.getPaymentStatus() == PaymentStatus.FAILED){
                int totalBookedTickets = findTotalTicketsBooked(booking);
                updateTicketTotal(totalBookedTickets, booking.getEventId());
                bookingRepository.delete(booking);
            } else {
                booking.setPaymentStatus(paymentStatusChangeDTO.getPaymentStatus());
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                log.info("Booking online or offline : {}", booking.isOnline());

                if (booking.isOnline()){
                    RoomAccessDTO roomAccessDTO = new RoomAccessDTO();
                    roomAccessDTO.setUserId(booking.getUserId());
                    roomAccessDTO.setUserEmail(booking.getEmail());
                    roomAccessDTO.setEventId(String.valueOf(booking.getEventId()));
                    communicationClient.allowRoom(roomAccessDTO);

                    log.info("Allow access to notified to communication service . //////////////////////////////////////////");
                }


                log.info("Payment status changed and notification details send successfully to notification service //////////////////////////////////////////");

                Event event = eventRepository.findById(UUID.fromString(booking.getEventId())).get();

                BookingCompletedNotificationDTO notificationDTO = new BookingCompletedNotificationDTO();
                notificationDTO.setUserName(booking.getUserName());
                notificationDTO.setEmail(booking.getEmail());
                notificationDTO.setTotalAmount(booking.getTotalAmount());
                notificationDTO.setTotalDiscount(booking.getTotalDiscount());
                notificationDTO.setPaymentStatus(booking.getPaymentStatus().name());
                notificationDTO.setEventName(event.getName());
                notificationDTO.setImageUrl(event.getImageUrl());

                notificationClient.sendBookingCompletedNotification(notificationDTO);
                log.info("Payment status changed and notification details send successfully to notification service");

            }
        }

    }

    public BookingInfoResponse getBookingInfoById(String id) {
        Booking booking = bookingRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return mapToBookingInfoResponse(booking);
    }

    public BookingInfoResponse getBookingInfoBySessionId(String sessionId) {
        Booking booking = bookingRepository.findByTempSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return mapToBookingInfoResponse(booking);
    }

    @Override
    public ResponseEntity<?> getEventBookedUsersByOrganizer(String eventId, Long organizerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Optional<Event> optionalEvent = eventRepository.findById(UUID.fromString(eventId));

        if (optionalEvent.isEmpty()) {
            return ResponseEntity.badRequest().body("Event not found.");
        }

        Event event = optionalEvent.get();

        if (!event.getOrganizerId().equals(organizerId)) {
            return ResponseEntity.badRequest().body("Something went wrong. Give correct info!");
        }

        Page<Booking> bookingsPage = bookingRepository.findByEventIdAndPaymentStatusIn(eventId, List.of(PaymentStatus.COMPLETED, PaymentStatus.NONE), pageable);

        BookedUsersListResponse response = BookedUsersListResponse.builder()
                .totalElements(bookingsPage.getTotalElements())
                .totalPages(bookingsPage.getTotalPages())
                .pageNumber(page)
                .pageSize(size)
                .bookedUsers(bookingsPage.getContent().stream()
                        .map(booking -> BookedUsersListResponse.BookedUsers.builder()
                                .userId(booking.getUserId())
                                .userName(booking.getUserName())
                                .email(booking.getEmail())
                                .address(booking.getAddress())
                                .country(booking.getCountry())
                                .state(booking.getState())
                                .city(booking.getCity())
                                .zipCode(booking.getZipCode())
                                .tickets(booking.getTickets())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getAllBookingsByUser(Long userId) {
        List<PaymentStatus> statuses = Arrays.asList(PaymentStatus.NONE, PaymentStatus.COMPLETED, PaymentStatus.REFUND_IN_PROGRESS, PaymentStatus.REFUND_PROCESSED);

        List<Booking> bookings = bookingRepository.findByUserIdAndPaymentStatusIn(userId, statuses);

        List<UserBookingInfo> userBookingInfos = bookings.stream().map(booking -> {
            Event event = eventRepository.findById(UUID.fromString(booking.getEventId()))
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            UserBookingInfo userBookingInfo = new UserBookingInfo();
            userBookingInfo.setBookingId(String.valueOf(booking.getId()));
            userBookingInfo.setUserId(userId);
            userBookingInfo.setEventId(booking.getEventId());
            userBookingInfo.setOnline(booking.isOnline());
            userBookingInfo.setEventName(event.getName());
            userBookingInfo.setImageUrl(event.getImageUrl());
            userBookingInfo.setTotalAmount(booking.getTotalAmount());
            userBookingInfo.setTotalDiscount(booking.getTotalDiscount());
            userBookingInfo.setBookingStatus(booking.getBookingStatus());
            userBookingInfo.setPaymentStatus(booking.getPaymentStatus());
            String startTimeAndDate = event.getTimeSlots().stream()
                    .findFirst()
                    .map(timeSlot -> timeSlot.getDate() + " " + timeSlot.getStartTime())
                    .orElse("No start time available");
            userBookingInfo.setStartTimeAndDate(startTimeAndDate);

            if (!event.isOnline()) {
                Booking.Ticket ticket = booking.getTickets().stream().findFirst().orElse(new Booking.Ticket());
                ticket.setTicketsInCategory(ticket.getTicketsInCategory());
                userBookingInfo.setTicket(ticket);
            } else {
                userBookingInfo.setTicket(null);
            }

            return userBookingInfo;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(userBookingInfos);
    }



    @Override
    public ResponseEntity<?> cancelBooking(String bookingId, Long userId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(UUID.fromString(bookingId));

        if (optionalBooking.isEmpty()) {
            log.error("Booking not found for bookingId: {}", bookingId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
        }

        Booking booking = optionalBooking.get();

        if (!booking.getUserId().equals(userId)) {
            log.error("User {} is not authorized to cancel booking with bookingId: {}", userId, bookingId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to cancel this booking.");
        }

        if (booking.getPaymentStatus() == PaymentStatus.COMPLETED) {
            Event event = eventRepository.findById(UUID.fromString(booking.getEventId())).get();
            OnlineEventSetting onlineEventSetting = event.getOnlineEventSetting();
            int refundPercentage = 100;

            if (!onlineEventSetting.isDisableRefunds()) {
                refundPercentage = Integer.parseInt(onlineEventSetting.getRefundPercentage());
            }

            RefundRequestDTO refundRequestDTO = RefundRequestDTO.builder()
                    .sessionId(booking.getPaymentSessionId())
                    .refundPercentage(refundPercentage)
                    .build();

            try {
                RefundDTO refundDTO = paymentClient.paymentRefundRequest(refundRequestDTO);

                assert refundDTO != null;
                if (refundDTO.getErrorMessage() != null) {
                    log.error("Error processing refund for bookingId: {}. Error: {}", bookingId, refundDTO.getErrorMessage());
                    throw new IllegalArgumentException("Error processing refund: " + refundDTO.getErrorMessage());
                }

                booking.setPaymentStatus(PaymentStatus.REFUND_IN_PROGRESS);

            } catch (HttpClientErrorException e) {
                log.error("Error calling payment service for bookingId: {}. HTTP Status: {}. Error: {}",
                        bookingId, e.getStatusCode(), e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error while calling payment service: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                log.error("Invalid request for refund for bookingId: {}. Error: {}", bookingId, e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid request for refund: " + e.getMessage());
            } catch (Exception e) {
                log.error("An unexpected error occurred while processing the refund for bookingId: {}. Error: {}",
                        bookingId, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An error occurred while processing the refund: " + e.getMessage());
            }
        }

        booking.setBookingStatus(BookingStatus.CANCELED);
        booking.setPaymentStatus(PaymentStatus.REFUND_IN_PROGRESS);

        try {
            bookingRepository.save(booking);
        } catch (Exception e) {
            log.error("Error saving canceled booking with bookingId: {}. Error: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving canceled booking: " + e.getMessage());
        }

        log.info("Booking with bookingId: {} has been canceled successfully.", bookingId);
        return ResponseEntity.status(HttpStatus.OK).body("Booking has been canceled successfully.");
    }



    private BookingInfoResponse mapToBookingInfoResponse(Booking booking) {

        Event event = eventRepository.findById(UUID.fromString(booking.getEventId()))
                .orElseThrow(() -> new RuntimeException("Event not found"));

        return BookingInfoResponse.builder()
                .id(booking.getId().toString())
                .userId(booking.getUserId())
                .eventId(booking.getEventId())
                .paymentSessionId(booking.getPaymentSessionId())
                .userName(booking.getUserName())
                .email(booking.getEmail())
                .address(booking.getAddress())
                .country(booking.getCountry())
                .state(booking.getState())
                .city(booking.getCity())
                .zipCode(booking.getZipCode())
                .totalAmount(booking.getTotalAmount())
                .totalDiscount(booking.getTotalDiscount())
                .isOnline(booking.isOnline())
                .paymentStatus(booking.getPaymentStatus())
                .tickets(booking.getTickets().stream().map(ticket -> BookingInfoResponse.BookingTicketDTO.builder()
                        .name(ticket.getName())
                        .quantity(ticket.getQuantity())
                        .ticketsInCategory(ticket.getTicketsInCategory())
                        .price(ticket.getPrice())
                        .discountPrice(ticket.getDiscountPrice())
                        .build()).collect(Collectors.toList()))
                .eventInfo(BookingInfoResponse.EventInfoDTO.builder()
                        .id(event.getId().toString())
                        .name(event.getName())
                        .imageUrl(event.getImageUrl())
                        .build())
                .build();
    }


    private int findTotalTicketsBooked(Booking booking) {
        if (booking.isOnline()) {
            return 1;
        }
        return booking.getTickets()
                .stream()
                .mapToInt(t -> t.getQuantity() * t.getTicketsInCategory())
                .sum();
    }

    private void updateTicketTotal(int count, String eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(UUID.fromString(eventId));
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            if (event.isOnline()) {
                OnlineEventTicket onlineEventTicket = event.getOnlineEventTicket();
                onlineEventTicket.setTotalTickets(onlineEventTicket.getTotalTickets() + count);
                event.setOnlineEventTicket(onlineEventTicket);
            } else {
                OfflineEventTickets offlineEventTickets = event.getOfflineEventTickets();
                offlineEventTickets.setTotalTickets(offlineEventTickets.getTotalTickets() + count);
                event.setOfflineEventTickets(offlineEventTickets);
            }
            eventRepository.save(event);
        } else {
            throw new NoSuchElementException("Event with ID " + eventId + " not found.");
        }
    }




}
