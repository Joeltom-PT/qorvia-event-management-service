package com.qorvia.eventmanagementservice.service;

import com.qorvia.eventmanagementservice.clients.CommunicationServiceClient;
import com.qorvia.eventmanagementservice.dto.*;
import com.qorvia.eventmanagementservice.dto.request.CreateOfflineEventDetailRequest;
import com.qorvia.eventmanagementservice.dto.request.CreateOfflineEventTicketRequest;
import com.qorvia.eventmanagementservice.dto.request.CreateOnlineEventDetailRequest;
import com.qorvia.eventmanagementservice.dto.request.EventCategoryRequest;
import com.qorvia.eventmanagementservice.dto.response.*;
import com.qorvia.eventmanagementservice.model.*;
import com.qorvia.eventmanagementservice.repository.BookingRepository;
import com.qorvia.eventmanagementservice.repository.EventApprovalRequestRepository;
import com.qorvia.eventmanagementservice.repository.EventCategoryRepository;
import com.qorvia.eventmanagementservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventCategoryRepository eventCategoryRepository;
    private final EventRepository eventRepository;
    private final EventApprovalRequestRepository eventApprovalRequestRepository;
    private final BookingRepository bookingRepository;
    private final CommunicationServiceClient communicationClient;

    @Override
    public ResponseEntity<?> categoryRequest(EventCategoryRequest categoryRequest) {
        try {
            if (categoryRequest.getName() == null || categoryRequest.getName().isEmpty()) {
                return ResponseEntity.badRequest().body("Category name cannot be null or empty");
            }
            if (categoryRequest.getDescription() == null || categoryRequest.getDescription().isEmpty()) {
                return ResponseEntity.badRequest().body("Category description cannot be null or empty");
            }

            Optional<EventCategory> existingCategoryOpt = eventCategoryRepository.findByNameIgnoreCase(categoryRequest.getName());

            if (existingCategoryOpt.isPresent()) {
                EventCategory existingCategory = existingCategoryOpt.get();
                if (existingCategory.getStatus() == CategoryStatus.REJECTED ||
                        existingCategory.getStatus() == CategoryStatus.INACTIVE) {
                    existingCategory.setStatus(CategoryStatus.PENDING);
                    eventCategoryRepository.save(existingCategory);
                }
                return ResponseEntity.ok("Category already exists.");
            }

            EventCategory eventCategory = EventCategory.builder()
                    .id(UUID.randomUUID())
                    .name(categoryRequest.getName())
                    .description(categoryRequest.getDescription())
                    .status(CategoryStatus.PENDING)
                    .build();

            EventCategory savedCategory = eventCategoryRepository.save(eventCategory);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the category: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> changeCategoryStatus(UUID id, String status) {
        try {
            Optional<EventCategory> optionalCategory = eventCategoryRepository.findById(id);
            if (optionalCategory.isPresent()) {
                EventCategory eventCategory = optionalCategory.get();
                eventCategory.setStatus(CategoryStatus.valueOf(status.toUpperCase()));
                EventCategory updatedCategory = eventCategoryRepository.save(eventCategory);
                return ResponseEntity.ok(updatedCategory);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value: " + status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the category status: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<GetAllCategoriesResponse> getAllEventCategories(int page, int size, String search, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        EventCategory probe = new EventCategory();

        if (status != null && !status.isEmpty()) {
            probe.setStatus(CategoryStatus.valueOf(status));
        }

        log.info("searching the values of search query: {} , and status : {}", search, status);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "description")
                .withMatcher("name", match -> match.contains().ignoreCase());

        if (search != null && !search.isEmpty()) {
            probe.setName(search);
        }

        Example<EventCategory> example = Example.of(probe, matcher);
        Page<EventCategory> eventCategoryPage = eventCategoryRepository.findAll(example, pageable);

        List<EventCategoryDTO> eventCategoryDTOs = eventCategoryPage.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        GetAllCategoriesResponse response = new GetAllCategoriesResponse();
        response.setCategories(eventCategoryDTOs);
        response.setTotalElements(eventCategoryPage.getTotalElements());
        response.setTotalPages(eventCategoryPage.getTotalPages());
        response.setPageNumber(eventCategoryPage.getNumber());
        response.setPageSize(eventCategoryPage.getSize());

        return ResponseEntity.ok(response);
    }

    @Override
    public List<ActiveEventCategoryDTO> getAllActiveCategories() {
        List<EventCategory> activeCategories = eventCategoryRepository.findByStatus(CategoryStatus.ACTIVE);

        if (activeCategories.isEmpty()) {
            log.warn("No active event categories found.");
            return Collections.emptyList();
        }

        return activeCategories.stream()
                .map(category -> new ActiveEventCategoryDTO(
                        category.getId(),
                        category.getName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> createOnlineEventDetail(CreateOnlineEventDetailRequest eventDetail, Long organizerId) {
        if (eventDetail.getId() != null && !eventDetail.getId().isEmpty()) {
            UUID eventId = UUID.fromString(eventDetail.getId());
            Optional<Event> existingEvent = eventRepository.findById(eventId);

            if (existingEvent.isPresent() && existingEvent.get().getOrganizerId() == organizerId) {
                Event eventToUpdate = existingEvent.get();
                eventToUpdate.setName(eventDetail.getName());
                eventToUpdate.setCategoryId(eventDetail.getCategoryId());
                eventToUpdate.setEventType(EventType.valueOf(eventDetail.getTypeName().toUpperCase()));
                eventToUpdate.setDescription(eventDetail.getDescription());
                eventToUpdate.setImageUrl(eventDetail.getImageUrl());
                eventToUpdate.setOnline(true);
                eventRepository.save(eventToUpdate);
                return ResponseEntity.ok(eventId);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
            }
        } else {
            Event newEvent = Event.builder()
                    .id(UUID.randomUUID())
                    .organizerId(organizerId)
                    .name(eventDetail.getName())
                    .categoryId(eventDetail.getCategoryId())
                    .eventType(EventType.valueOf(eventDetail.getTypeName().toUpperCase()))
                    .description(eventDetail.getDescription())
                    .imageUrl(eventDetail.getImageUrl())
                    .eventFormStatus(EventFormStatus.PENDING)
                    .eventState(EventState.DRAFT)
                    .approvalStatus(null)
                    .isOnline(true)
                    .build();
            Event savedEvent = eventRepository.save(newEvent);
            return ResponseEntity.ok(savedEvent.getId());
        }
    }

    @Override
    public ResponseEntity<?> createOnlineEventTimeSlots(String eventId, List<EventTimeSlotDTO> timeSlotDTOs, Long organizerId) {
        try {
            UUID uuid = UUID.fromString(eventId);
            Optional<Event> optionalEvent = eventRepository.findById(uuid);

            if (!optionalEvent.isPresent() && optionalEvent.get().getOrganizerId() == organizerId) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
            }

            Event event = optionalEvent.get();

            List<EventTimeSlot> timeSlots = timeSlotDTOs.stream()
                    .map(dto -> EventTimeSlot.builder()
                            .id(UUID.randomUUID())
                            .date(dto.getDate())
                            .startTime(dto.getStartTime())
                            .endTime(dto.getEndTime())
                            .duration(dto.getDuration())
                            .eventId(uuid)
                            .build())
                    .collect(Collectors.toList());

            event.setTimeSlots(timeSlots);

            eventRepository.save(event);

            return ResponseEntity.status(HttpStatus.CREATED).body("Time slots created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format");
        } catch (Exception e) {
            log.error("Error creating time slots for event {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating time slots: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> createOnlineEventTicket(String eventId,  Long organizerId, OnlineEventTicketDTO onlineEventTicketDTO) {
        try {
            UUID uuid = UUID.fromString(eventId);
            Optional<Event> optionalEvent = eventRepository.findById(uuid);
            if (!optionalEvent.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
            }
            Event event = optionalEvent.get();

            if (!event.getOrganizerId().equals(organizerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            OnlineEventTicket ticket = OnlineEventTicket.builder()
                    .totalTickets(Optional.ofNullable(onlineEventTicketDTO.getTotalTickets())
                            .map(Integer::parseInt)
                            .orElse(null))
                    .isFreeEvent(onlineEventTicketDTO.isFreeEvent())
                    .price(Optional.ofNullable(onlineEventTicketDTO.getPrice())
                            .map(Integer::parseInt)
                            .orElse(null))
                    .hasEarlyBirdDiscount(onlineEventTicketDTO.isHasEarlyBirdDiscount())
                    .discountType(onlineEventTicketDTO.getDiscountType())
                    .discountValue(Optional.ofNullable(onlineEventTicketDTO.getDiscountValue())
                            .map(Integer::parseInt)
                            .orElse(null))
                    .build();

            event.setOnlineEventTicket(ticket);

            eventRepository.save(event);

            return ResponseEntity.status(HttpStatus.CREATED).body("Online event ticket created successfully");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid number format in ticket information: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format");
        } catch (Exception e) {
            log.error("Error creating online event ticket for event {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the online event ticket: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> createOnlineEventSettings(String eventId, Long organizerId, OnlineEventSettingDTO onlineEventSettingDto) {
        try {
            UUID uuid = UUID.fromString(eventId);
            Optional<Event> optionalEvent = eventRepository.findById(uuid);

            if (!optionalEvent.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
            }

            Event event = optionalEvent.get();

            if (!event.getOrganizerId().equals(organizerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            OnlineEventSetting setting = OnlineEventSetting.builder()
                    .startImmediately(onlineEventSettingDto.isStartImmediately())
                    .bookingStartDate(onlineEventSettingDto.getBookingStartDate())
                    .bookingStartTime(onlineEventSettingDto.getBookingStartTime())
                    .continueUntilEvent(onlineEventSettingDto.isContinueUntilEvent())
                    .bookingEndDate(onlineEventSettingDto.getBookingEndDate())
                    .bookingEndTime(onlineEventSettingDto.getBookingEndTime())
                    .disableRefunds(onlineEventSettingDto.isDisableRefunds())
                    .refundPercentage(onlineEventSettingDto.getRefundPercentage())
                    .refundPolicy(onlineEventSettingDto.getRefundPolicy())
                    .build();

            event.setOnlineEventSetting(setting);
            if (event.getEventFormStatus() == EventFormStatus.PENDING) {
                event.setEventFormStatus(EventFormStatus.COMPLETED);
            }
            eventRepository.save(event);

            return ResponseEntity.status(HttpStatus.CREATED).body("Online event settings created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format");
        } catch (Exception e) {
            log.error("Error creating online event settings for event {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the online event settings: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getOnlineEventDetail(String id, Long organizerId) {
        try {
            UUID eventId = UUID.fromString(id);
            Optional<Event> eventOptional = eventRepository.findById(eventId);

            if (eventOptional.isPresent() && eventOptional.get().getOrganizerId() == organizerId) {
                Event event = eventOptional.get();
                GetOnlineEventDetailResponse response = new GetOnlineEventDetailResponse();
                response.setId(event.getId().toString());
                response.setName(event.getName());
                response.setCategoryId(event.getCategoryId());
                response.setEventType(event.getEventType());
                response.setDescription(event.getDescription());
                response.setImageUrl(event.getImageUrl());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid UUID format", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> getOnlineEventTimeSlots(String eventId, Long organizerId) {
        try {
            UUID uuid = UUID.fromString(eventId);
            Optional<Event> optionalEvent = eventRepository.findById(uuid);

            if (!optionalEvent.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
            }

            Event event = optionalEvent.get();

            if (!event.getOrganizerId().equals(organizerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            List<EventTimeSlotDTO> timeSlotDTOs = (event.getTimeSlots() != null ? event.getTimeSlots() : new ArrayList<EventTimeSlot>()).stream()
                    .map(this::convertToTimeSlotDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(timeSlotDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format");
        } catch (Exception e) {
            log.error("Error retrieving time slots for event {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving time slots: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getOnlineEventTicket(String eventId, Long organizerId) {
        try {
            UUID uuid = UUID.fromString(eventId);
            Optional<Event> optionalEvent = eventRepository.findById(uuid);

            if (!optionalEvent.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
            }

            Event event = optionalEvent.get();

            if (!event.getOrganizerId().equals(organizerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            OnlineEventTicket ticket = event.getOnlineEventTicket();

            if (ticket == null) {
                return ResponseEntity.ok(null);
            }

            OnlineEventTicketDTO ticketDTO = convertToOnlineEventTicketDTO(ticket);

            return ResponseEntity.ok(ticketDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format");
        } catch (Exception e) {
            log.error("Error retrieving online event ticket for event {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving the online event ticket: " + e.getMessage());
        }
    }


    // getOnlineEventSettings Is not implemented , Add it


    @Override
    public ResponseEntity<?> createOfflineEventDetail(String eventId, CreateOfflineEventDetailRequest offlineEventDetailRequest, Long organizerId) {
        Event event;

        if (eventId == null) {
            event = new Event();
            event.setId(UUID.randomUUID());
            event.setEventState(EventState.DRAFT);
            event.setEventFormStatus(EventFormStatus.PENDING);
            event.setApprovalStatus(null);
            event.setOnline(false);
            event.setDeleted(false);
        } else {
            Optional<Event> existingEventOptional = eventRepository.findById(UUID.fromString(eventId));
            if (existingEventOptional.isPresent()) {
                event = existingEventOptional.get();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Event with ID " + eventId + " not found");
            }
        }
        updateEventFromRequest(event, offlineEventDetailRequest, organizerId);

        event = eventRepository.save(event);
        return ResponseEntity.ok(event.getId());
    }

    @Override
    public List<OrganizerEventListingPageResponse> getEventsByOrganizer(Long organizerId) {
        try {
            List<Event> events = eventRepository.findByOrganizerIdAndIsDeletedFalse(organizerId);

            if (events.isEmpty()) {
                return null;
            }

            List<OrganizerEventListingPageResponse> eventDTOs = events.stream().map(event -> {
                OrganizerEventListingPageResponse eventDTO = new OrganizerEventListingPageResponse();
                eventDTO.setId(event.getId().toString());
                eventDTO.setName(event.getName());
                eventDTO.setEventType(String.valueOf(event.getEventType()));
                eventDTO.setImageUrl(event.getImageUrl());
                eventDTO.setIsOnline(event.isOnline());
                eventDTO.setIsDeleted(event.isDeleted());
                eventDTO.setEventFormStatus(String.valueOf(event.getEventFormStatus()));
                eventDTO.setEventState(String.valueOf(event.getEventState()));
                if (event.getTimeSlots() != null && !event.getTimeSlots().isEmpty() && event.getTimeSlots().get(0) != null && event.getTimeSlots().get(0).getDate() != null) {
                    eventDTO.setStartDate(event.getTimeSlots().get(0).getDate());
                } else {
                    eventDTO.setStartDate(null);
                }
                if (event.isOnline()) {
                    if (event.getOnlineEventTicket() != null && event.getOnlineEventTicket().getTotalTickets() != null) {
                        eventDTO.setTotalTickets(event.getOnlineEventTicket().getTotalTickets());
                    } else {
                        eventDTO.setTotalTickets(0);
                    }
                } else {
                    if (event.getOfflineEventTickets() != null && event.getOfflineEventTickets().getTotalTickets() != null) {
                        eventDTO.setTotalTickets(event.getOfflineEventTickets().getTotalTickets());
                    } else {
                        eventDTO.setTotalTickets(0);
                    }
                }
                eventDTO.setApprovalStatus(String.valueOf(event.getApprovalStatus()));
                eventDTO.setCategory(findCategoryById(event.getCategoryId()));
                return eventDTO;
            }).collect(Collectors.toList());

            eventDTOs.forEach(eventDTO -> log.info("EventDTO: {}", eventDTO));


            return eventDTOs;

        } catch (Exception e) {
            log.error("Error retrieving events for organizer {}: {}", organizerId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public ResponseEntity<OrganizerEventListingPageResponse> getSpecificEventByOrganizerId(String eventIdStr, Long organizerId) {
        try {
            UUID eventId = UUID.fromString(eventIdStr);
            Optional<Event> eventOptional = eventRepository.findByIdAndOrganizerId(eventId, organizerId);

            if (eventOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Event event = eventOptional.get();
            OrganizerEventListingPageResponse eventDTO = new OrganizerEventListingPageResponse();

            eventDTO.setId(event.getId().toString());
            eventDTO.setName(event.getName());
            eventDTO.setEventType(String.valueOf(event.getEventType()));
            eventDTO.setImageUrl(event.getImageUrl());
            eventDTO.setIsOnline(event.isOnline());
            eventDTO.setIsDeleted(event.isDeleted());
            eventDTO.setEventFormStatus(String.valueOf(event.getEventFormStatus()));
            eventDTO.setEventState(String.valueOf(event.getEventState()));

            if (event.getTimeSlots() != null && !event.getTimeSlots().isEmpty()) {
                EventTimeSlot firstTimeSlot = event.getTimeSlots().get(0);
                EventTimeSlot lastTimeSlot = event.getTimeSlots().get(event.getTimeSlots().size() - 1);

                if (firstTimeSlot.getDate() != null && firstTimeSlot.getStartTime() != null) {
                    String startDateAndTime = firstTimeSlot.getDate() + " " + firstTimeSlot.getStartTime();
                    eventDTO.setStartDateAndTime(startDateAndTime);
                } else {
                    eventDTO.setStartDateAndTime(null);
                }

                if (lastTimeSlot.getDate() != null && lastTimeSlot.getEndTime() != null) {
                    String endDateAndTime = lastTimeSlot.getDate() + " " + lastTimeSlot.getEndTime();
                    eventDTO.setEndDateAndTime(endDateAndTime);
                } else {
                    eventDTO.setEndDateAndTime(null);
                }

                if (firstTimeSlot.getStartTime() != null && lastTimeSlot.getEndTime() != null) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    Date startDateTime = timeFormat.parse(firstTimeSlot.getStartTime());
                    Date endDateTime = timeFormat.parse(lastTimeSlot.getEndTime());

                    long durationMillis = endDateTime.getTime() - startDateTime.getTime();
                    long durationHours = durationMillis / (1000 * 60 * 60);
                    long durationMinutes = (durationMillis / (1000 * 60)) % 60;

                    String duration = durationHours + "h " + durationMinutes + "m";
                    eventDTO.setDuration(duration);
                } else {
                    eventDTO.setDuration(null);
                }
            } else {
                eventDTO.setStartDateAndTime(null);
                eventDTO.setEndDateAndTime(null);
                eventDTO.setDuration(null);
            }

            if (event.isOnline()) {
                if (event.getOnlineEventTicket() != null && event.getOnlineEventTicket().getTotalTickets() != null) {
                    eventDTO.setTotalTickets(event.getOnlineEventTicket().getTotalTickets());
                } else {
                    eventDTO.setTotalTickets(0);
                }
            } else {
                if (event.getOfflineEventTickets() != null && event.getOfflineEventTickets().getTotalTickets() != null) {
                    eventDTO.setTotalTickets(event.getOfflineEventTickets().getTotalTickets());
                } else {
                    eventDTO.setTotalTickets(0);
                }
            }

            eventDTO.setApprovalStatus(String.valueOf(event.getApprovalStatus()));
            eventDTO.setCategory(findCategoryById(event.getCategoryId()));

            return ResponseEntity.ok(eventDTO);

        } catch (Exception e) {
            log.error("Error retrieving specific event {} for organizer {}: {}", eventIdStr, organizerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<?> requestAdminApprovalForEvent(String eventId, Long organizerId) {
        log.info("Working on the event approval request with event Id {}, and organizer Id {}", eventId, organizerId);

        Optional<Event> optionalEvent = eventRepository.findById(UUID.fromString(eventId));
        if (optionalEvent.isPresent() && optionalEvent.get().getOrganizerId() == organizerId) {
            Event event = optionalEvent.get();
            event.setApprovalStatus(AdminApprovalStatus.PENDING);
            eventRepository.save(event);
        }

        EventApprovalRequests approvalRequests = new EventApprovalRequests();
        approvalRequests.setId(UUID.randomUUID());
        approvalRequests.setApprovalStatus(AdminApprovalStatus.PENDING);
        approvalRequests.setEventId(UUID.fromString(eventId));

        try {
            eventApprovalRequestRepository.save(approvalRequests);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Approval request for event " + eventId + " has been submitted successfully.");
        } catch (Exception e) {
            log.error("Error saving approval request for event Id {}: {}", eventId, e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to submit approval request for event " + eventId + ".");
        }
    }



    @Override
    public ResponseEntity<?> deleteEvent(String eventId, Long organizerId) {
        try {
            Optional<Event> optionalEvent = eventRepository.findById(UUID.fromString(eventId));
            if (optionalEvent.isPresent() && !optionalEvent.get().isDeleted() && optionalEvent.get().getOrganizerId() == organizerId && optionalEvent.get().getEventState() == EventState.DRAFT) {
                Event event = optionalEvent.get();
                event.setDeleted(true);
                eventRepository.save(event);
                return ResponseEntity.ok("Event deleted");
            } else {
                return ResponseEntity.badRequest().body("Event Id with organizer is not match");
            }
        } catch (Exception e) {
            ResponseEntity.badRequest().body("Some error occur while deleting the event");
        }
        return ResponseEntity.badRequest().body("Something went wrong");
    }

    @Override
    public ResponseEntity<?> withdrawEventApprovalRequest(String eventId, Long organizerId) {
        Optional<Event> optionalEvent = eventRepository.findByIdAndOrganizerId(UUID.fromString(eventId), organizerId);
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();

            if (event.getApprovalStatus() == AdminApprovalStatus.PENDING) {
                event.setApprovalStatus(null);
                eventRepository.save(event);

                eventApprovalRequestRepository.deleteByEventId(UUID.fromString(eventId));

                return ResponseEntity.ok("Event approval request withdrawn successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Cannot withdraw approval request. Event status is not PENDING.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Event not found for the given eventId and organizerId.");
        }
    }

    @Override
    public ResponseEntity<?> getEventDetails(String eventId) {

        Optional<Event> eventOptional = eventRepository.findById(UUID.fromString(eventId));
        if (eventOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        Event event = eventOptional.get();

        EventDto eventDto = EventDto.builder()
                .id(event.getId())
                .organizerId(event.getOrganizerId())
                .eventState(event.getEventState() != null ? event.getEventState().name() : null)
                .name(event.getName())
                .categoryId(event.getCategoryId())
                .eventType(event.getEventType() != null ? event.getEventType().name() : null)
                .isOnline(event.isOnline())
                .description(event.getDescription())
                .imageUrl(event.getImageUrl())
                .isBlocked(event.isBlocked())
                .address(event.getAddress())
                .lan(event.getLan())
                .lon(event.getLon())
                .eventFormStatus(event.getEventFormStatus() != null ? event.getEventFormStatus().name() : null)
                .approvalStatus(event.getApprovalStatus() != null ? event.getApprovalStatus().name() : null)
                .isDeleted(event.isDeleted())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .timeSlots(event.getTimeSlots() != null ? event.getTimeSlots().stream()
                        .map(slot -> EventDto.EventTimeSlotDto.builder()
                                .id(slot.getId())
                                .eventId(slot.getEventId())
                                .date(slot.getDate())
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .duration(slot.getDuration())
                                .build())
                        .toList() : null)
                .onlineEventTicket(event.getOnlineEventTicket() != null ? EventDto.OnlineEventTicketDto.builder()
                        .totalTickets(event.getOnlineEventTicket().getTotalTickets())
                        .isFreeEvent(event.getOnlineEventTicket().isFreeEvent())
                        .price(event.getOnlineEventTicket().getPrice())
                        .hasEarlyBirdDiscount(event.getOnlineEventTicket().isHasEarlyBirdDiscount())
                        .discountType(event.getOnlineEventTicket().getDiscountType())
                        .discountValue(event.getOnlineEventTicket().getDiscountValue())
                        .build() : null)
                .offlineEventTickets(event.getOfflineEventTickets() != null ? EventDto.OfflineEventTicketsDto.builder()
                        .totalTickets(event.getOfflineEventTickets().getTotalTickets())
                        .categories(event.getOfflineEventTickets().getCategories() != null ? event.getOfflineEventTickets().getCategories().stream()
                                .map(category -> EventDto.OfflineEventTicketsDto.TicketCategoryDto.builder()
                                        .name(category.getName())
                                        .totalTickets(category.getTotalTickets())
                                        .price(category.getPrice())
                                        .discountType(category.getDiscountType())
                                        .discountValue(category.getDiscountValue())
                                        .description(category.getDescription())
                                        .build())
                                .toList() : null)
                        .build() : null)
                .EventSettingDto(event.getOnlineEventSetting() != null ? EventDto.EventSettingDto.builder()
                        .startImmediately(event.getOnlineEventSetting().isStartImmediately())
                        .bookingStartDate(event.getOnlineEventSetting().getBookingStartDate())
                        .bookingStartTime(event.getOnlineEventSetting().getBookingStartTime())
                        .continueUntilEvent(event.getOnlineEventSetting().isContinueUntilEvent())
                        .bookingEndDate(event.getOnlineEventSetting().getBookingEndDate())
                        .bookingEndTime(event.getOnlineEventSetting().getBookingEndTime())
                        .disableRefunds(event.getOnlineEventSetting().isDisableRefunds())
                        .refundPercentage(event.getOnlineEventSetting().getRefundPercentage())
                        .refundPolicy(event.getOnlineEventSetting().getRefundPolicy())
                        .build() : null)
                .build();

        log.info("getting the event details in admin event detail api call. response is with event Id : {}", eventDto.getId());

        return ResponseEntity.ok(eventDto);
    }

    public ResponseEntity<?> getAllApprovedEvents(int page, int size, String search, String eventState, Boolean isOnline, String categoryId) {
        Pageable pageable = PageRequest.of(page, size);
        AdminApprovalStatus approvalStatus = AdminApprovalStatus.ACCEPTED;

        Page<Event> eventPage = eventRepository.findAllApprovedEvents(eventState, isOnline, categoryId, approvalStatus, search, pageable);

        List<GetAllApprovedEventsResponse.EventInfo> eventInfos = eventPage.getContent().stream()
                .map(event -> {
                    String startDateAndTime = null;
                    String endDateAndTime = null;

                    if (event.getTimeSlots() != null && !event.getTimeSlots().isEmpty()) {
                        EventTimeSlot firstTimeSlot = event.getTimeSlots().stream()
                                .min(Comparator.comparing(EventTimeSlot::getStartTime))
                                .orElse(null);

                        EventTimeSlot lastTimeSlot = event.getTimeSlots().stream()
                                .max(Comparator.comparing(EventTimeSlot::getEndTime))
                                .orElse(null);

                        if (firstTimeSlot != null) {
                            startDateAndTime = firstTimeSlot.getDate() + " " + firstTimeSlot.getStartTime();
                        }
                        if (lastTimeSlot != null) {
                            endDateAndTime = lastTimeSlot.getDate() + " " + lastTimeSlot.getEndTime();
                        }
                    }

                    return GetAllApprovedEventsResponse.EventInfo.builder()
                            .eventId(event.getId())
                            .eventName(event.getName())
                            .eventState(event.getEventState())
                            .isOnline(event.isOnline())
                            .eventCategory(findCategoryById(event.getCategoryId()))
                            .startDateAndTime(startDateAndTime)
                            .endDateAndTime(endDateAndTime)
                            .build();
                })
                .collect(Collectors.toList());

        GetAllApprovedEventsResponse response = GetAllApprovedEventsResponse.builder()
                .events(eventInfos)
                .totalElements(eventPage.getTotalElements())
                .totalPages(eventPage.getTotalPages())
                .pageNumber(page)
                .pageSize(size)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getAllEvents(int page, int size, String search, Boolean isOnline, String categoryId, String organizerId, String date) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Event> eventPage = eventRepository.findAllEvents(search, isOnline, categoryId, organizerId, date, pageable);

        List<getAllEventsResponse.EventInfo> eventInfos = eventPage.getContent().stream()
                .map(event -> {
                    String startDateAndTime = null;

                    if (event.getTimeSlots() != null && !event.getTimeSlots().isEmpty()) {
                        EventTimeSlot earliestTimeSlot = event.getTimeSlots().stream()
                                .min(Comparator.comparing(EventTimeSlot::getStartTime))
                                .orElse(null);

                        if (earliestTimeSlot != null) {
                            startDateAndTime = earliestTimeSlot.getDate() + " " + earliestTimeSlot.getStartTime();
                        }
                    }

                    return getAllEventsResponse.EventInfo.builder()
                            .eventId(event.getId())
                            .organizerId(event.getOrganizerId())
                            .eventName(event.getName())
                            .eventState(event.getEventState())
                            .imgUrl(event.getImageUrl())
                            .eventDescription(event.getDescription())
                            .isOnline(event.isOnline())
                            .eventCategory(findCategoryById(event.getCategoryId()))
                            .startDateAndTime(startDateAndTime)
                            .build();
                })
                .collect(Collectors.toList());

        getAllEventsResponse response = getAllEventsResponse.builder()
                .events(eventInfos)
                .totalElements(eventPage.getTotalElements())
                .totalPages(eventPage.getTotalPages())
                .pageNumber(page)
                .pageSize(size)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getOnlineEventData(String eventId, Long userId) {
        UUID eventUUID = UUID.fromString(eventId);
        boolean isRegistered = false;
        if (userId != null) {
            List<Booking> bookings = bookingRepository.findAllByEventIdAndUserId(eventId, userId);
            if (bookings != null && !bookings.isEmpty()) {
                isRegistered = true;
            }
        }

        Event event = eventRepository.findById(eventUUID).orElse(null);

        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        if (!event.isOnline()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The event is not an online event");
        }

        OnlineEventTicket onlineEventTicket = event.getOnlineEventTicket();
        List<EventTimeSlot> timeSlots = event.getTimeSlots();
        OnlineEventSetting onlineEventSetting = event.getOnlineEventSetting();

        Long organizerId = event.getOrganizerId();
//        String organizerName = getOrganizerName(organizerId);
        String categoryId = event.getCategoryId();
        String categoryName = findCategoryById(categoryId);

        OnlineEventDTO onlineEventDTO = OnlineEventDTO.builder()
                .name(event.getName())
                .description(event.getDescription())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .isOnline(event.isOnline())
                .imageUrl(event.getImageUrl())
                .organizerId(organizerId)
                .isRegistered(isRegistered)
                .onlineEventTicket(onlineEventTicket)
                .timeSlots(timeSlots)
                .onlineEventSetting(onlineEventSetting)
                .build();

        return ResponseEntity.ok(onlineEventDTO);
    }


    @Override
    public ResponseEntity<?> getOfflineEventData(String eventId, Long userId) {
        UUID eventUUID = UUID.fromString(eventId);
        boolean isBooked = false;
        if (userId != null) {
            List<Booking> bookings = bookingRepository.findAllByEventIdAndUserId(eventId, userId);
            if (bookings != null && !bookings.isEmpty()) {
                isBooked = true;
            }
        }

        Event event = eventRepository.findById(eventUUID).orElse(null);

        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        if (event.isOnline()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The event is not an offline event");
        }

        OfflineEventTickets offlineEventTickets = event.getOfflineEventTickets();
        List<EventTimeSlot> timeSlots = event.getTimeSlots();
        String categoryId = event.getCategoryId();
        String categoryName = findCategoryById(categoryId);

        OfflineEventDTO offlineEventDTO = OfflineEventDTO.builder()
                .eventId(event.getId().toString())
                .name(event.getName())
                .description(event.getDescription())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .isOnline(event.isOnline())
                .isBooked(isBooked)
                .imageUrl(event.getImageUrl())
                .address(event.getAddress())
                .lan(event.getLan())
                .lon(event.getLon())
                .organizerId(event.getOrganizerId())
                .offlineEventTickets(offlineEventTickets)
                .timeSlots(timeSlots)
                .onlineEventSetting(event.getOnlineEventSetting())
                .build();

        return ResponseEntity.ok(offlineEventDTO);
    }


    @Override
    public ResponseEntity<?> getEventSettings(String eventId, Long organizerId) {
        UUID eventUUID = UUID.fromString(eventId);

        Event event = eventRepository.findById(eventUUID).orElse(null);

        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        if (!event.getOrganizerId().equals(organizerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        OnlineEventSetting onlineEventSetting = event.getOnlineEventSetting();
        if (onlineEventSetting == null) {
            return ResponseEntity.ok(null);
        }

        OnlineEventSettingDTO eventSettingDTO = OnlineEventSettingDTO.builder()
                .startImmediately(onlineEventSetting.isStartImmediately())
                .bookingStartDate(onlineEventSetting.getBookingStartDate() != null ? onlineEventSetting.getBookingStartDate() : "")
                .bookingStartTime(onlineEventSetting.getBookingStartTime() != null ? onlineEventSetting.getBookingStartTime() : "")
                .continueUntilEvent(onlineEventSetting.isContinueUntilEvent())
                .bookingEndDate(onlineEventSetting.getBookingEndDate() != null ? onlineEventSetting.getBookingEndDate() : "")
                .bookingEndTime(onlineEventSetting.getBookingEndTime() != null ? onlineEventSetting.getBookingEndTime() : "")
                .disableRefunds(onlineEventSetting.isDisableRefunds())
                .refundPercentage(onlineEventSetting.getRefundPercentage() != null ? onlineEventSetting.getRefundPercentage() : "0")
                .refundPolicy(onlineEventSetting.getRefundPolicy() != null ? onlineEventSetting.getRefundPolicy() : "")
                .build();

        return ResponseEntity.ok(eventSettingDTO);
    }

    @Override
    public ResponseEntity<List<RegisteredEventDTO>> getAllRegisteredEventsByUserId(Long userId) {
        List<PaymentStatus> statuses = Arrays.asList(PaymentStatus.NONE, PaymentStatus.COMPLETED);

        List<Booking> bookings = bookingRepository.findByUserIdAndPaymentStatusIn(userId, statuses);

        List<UUID> eventIds = bookings.stream()
                .map(Booking::getEventId)
                .map(UUID::fromString)
                .distinct()
                .toList();

        List<Event> events = eventRepository.findAllById(eventIds);

        List<RegisteredEventDTO> registeredEventDTOs = events.stream()
                .map(event -> {
                    RegisteredEventDTO dto = new RegisteredEventDTO();
                    dto.setId(event.getId().toString());
                    dto.setName(event.getName());
                    dto.setOnline(event.isOnline());
                    dto.setImageUrl(event.getImageUrl());
                    if (!event.getTimeSlots().isEmpty()) {
                        EventTimeSlot firstSlot = event.getTimeSlots().get(0);
                        dto.setStartDateAndTime(firstSlot.getDate() + " " + firstSlot.getStartTime());
                        dto.setEndDateAndTime(firstSlot.getDate() + " " + firstSlot.getEndTime());
                    } else {
                        dto.setStartDateAndTime(null);
                        dto.setEndDateAndTime(null);
                    }
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(registeredEventDTOs);
    }

    @Override
    public ResponseEntity<List<LiveEvent>> getAllLive(Long organizerId) {
        LocalDateTime now = LocalDateTime.now();

        List<Event> events = eventRepository.findByOrganizerIdAndIsDeletedFalse(organizerId);

        List<LiveEvent> liveEvents = events.stream()
                .filter(event -> event.getTimeSlots().stream().anyMatch(slot -> {
                    LocalDateTime startDateTime = LocalDateTime.parse(slot.getDate() + "T" + slot.getStartTime());
                    LocalDateTime endDateTime = LocalDateTime.parse(slot.getDate() + "T" + slot.getEndTime());
                    return now.isAfter(startDateTime) && now.isBefore(endDateTime);
                }))
                .map(event -> {
                    LiveEvent liveEvent = new LiveEvent();
                    liveEvent.setId(event.getId().toString());
                    liveEvent.setName(event.getName());
                    liveEvent.setStartDateAndTime(event.getTimeSlots().get(0).getDate() + "T" + event.getTimeSlots().get(0).getStartTime());
                    liveEvent.setEndDateAndTime(event.getTimeSlots().get(0).getDate() + "T" + event.getTimeSlots().get(0).getEndTime());
                    liveEvent.setImageUrl(event.getImageUrl());
                    liveEvent.setOnline(event.isOnline());
                    return liveEvent;
                })
                .toList();

        return ResponseEntity.ok(liveEvents);
    }

    @Override
    public ResponseEntity<?> getFeaturedEvents(String eventId, int count) {
        List<Event> events = eventRepository.getFeaturedEvents(UUID.fromString(eventId), count);

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No featured events found");
        }
        List<EventInfoDTO> eventInfoDTOs = events.stream()
                .map(event -> {
                    EventTimeSlot firstTimeSlot = event.getTimeSlots().isEmpty()
                            ? null
                            : event.getTimeSlots().get(0);

                    return EventInfoDTO.builder()
                            .eventId(event.getId())
                            .organizerId(event.getOrganizerId())
                            .eventName(event.getName())
                            .imageUrl(event.getImageUrl())
                            .isOnline(event.isOnline())
                            .eventCategory(findCategoryById(event.getCategoryId()))
                            .startDateAndTime(firstTimeSlot != null
                                    ? firstTimeSlot.getDate() + " " + firstTimeSlot.getStartTime()
                                    : null)
                            .endDateAndTime(firstTimeSlot != null
                                    ? firstTimeSlot.getDate() + " " + firstTimeSlot.getEndTime()
                                    : null)
                            .build();
                })
                .toList();

        return ResponseEntity.ok(eventInfoDTOs);
    }

    @Override
    public ResponseEntity<?> getUserCalendar(long userId) {
        try {
            List<Booking> userBookings = bookingRepository.findByUserIdAndBookingStatus(
                    userId,
                    BookingStatus.CONFIRMED
            );

            List<CalendarDataDTO> calendarData = new ArrayList<>();

            for (Booking booking : userBookings) {
                Event event = eventRepository.findById(UUID.fromString(booking.getEventId()))
                        .orElse(null);

                if (event != null && !event.isDeleted() && event.getTimeSlots() != null) {
                    for (EventTimeSlot timeSlot : event.getTimeSlots()) {
                        CalendarDataDTO calendarEntry = CalendarDataDTO.builder()
                                .eventId(event.getId().toString())
                                .organizerId(event.getOrganizerId())
                                .name(event.getName())
                                .startTimeAndDate(timeSlot.getDate() + " " + timeSlot.getStartTime())
                                .endTimeAndDate(timeSlot.getDate() + " " + timeSlot.getEndTime())
                                .imageUrl(event.getImageUrl())
                                .isOnline(event.isOnline())
                                .build();

                        calendarData.add(calendarEntry);
                    }
                }
            }

            calendarData.sort((a, b) -> {
                LocalDateTime dateTimeA = LocalDateTime.parse(a.getStartTimeAndDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                LocalDateTime dateTimeB = LocalDateTime.parse(b.getStartTimeAndDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return dateTimeA.compareTo(dateTimeB);
            });

            return ResponseEntity.ok(calendarData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching calendar " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getOrganizerCalender(long organizerId) {
        try {
            List<CalendarDataDTO> calendarData = eventRepository.getOrganizerCalender(organizerId);
            return ResponseEntity.ok(calendarData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while fetching organizer calendar: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getProfileEvents(Long organizerId) {
        List<Event> events = eventRepository.findTop5FilteredEventsByOrganizer(organizerId);

        List<ProfileEventDTO> profileEventDTOs = events.stream()
                .map(this::mapToProfileEventDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(profileEventDTOs);
    }

    @Override
    public List<GetEventInfoDTO> getEventsByIds(List<String> ids) {
        return ids.stream()
                .map(this::mapEventToDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> getEventStatistics(Long organizerId) {
        List<Event> eventList = eventRepository.findByOrganizerId(organizerId);
        if (eventList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Events not found.");
        }

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        int totalEvents = eventList.size();
        int todayEvents = (int) eventList.stream()
                .filter(event -> event.getTimeSlots().stream()
                        .anyMatch(slot -> today.format(formatter).equals(slot.getDate())))
                .count();

        int thisWeekEvents = (int) eventList.stream()
                .filter(event -> event.getTimeSlots().stream()
                        .anyMatch(slot -> {
                            LocalDate eventDate = LocalDate.parse(slot.getDate(), formatter);
                            return !eventDate.isBefore(startOfWeek) && !eventDate.isAfter(today);
                        }))
                .count();

        EventStatisticsDTO statisticsDTO = new EventStatisticsDTO();
        statisticsDTO.setTotalEvents(totalEvents);
        statisticsDTO.setTodayEvents(todayEvents);
        statisticsDTO.setThisWeekEvents(thisWeekEvents);

        return ResponseEntity.ok(statisticsDTO);
    }


    private Optional<GetEventInfoDTO> mapEventToDTO(String id) {
        return eventRepository.findById(UUID.fromString(id))
                .map(event -> {
                    GetEventInfoDTO dto = new GetEventInfoDTO();
                    dto.setEventId(event.getId().toString());
                    dto.setOrganizerId(event.getOrganizerId());
                    dto.setCategory(findCategoryById(event.getCategoryId()));
                    dto.setTitle(event.getName());
                    dto.setImageUrl(event.getImageUrl());
                    dto.setDate(event.getTimeSlots().get(0).getDate());
                    return dto;
                });
    }


    private ProfileEventDTO mapToProfileEventDTO(Event event) {
        return ProfileEventDTO.builder()
                .eventId(event.getId())
                .organizerId(event.getOrganizerId())
                .eventName(event.getName())
                .imgUrl(event.getImageUrl())
                .eventDescription(event.getDescription())
                .isOnline(event.isOnline())
                .eventCategory(findCategoryById(event.getCategoryId()))
                .startDateAndTime(getFirstTimeSlotDateAndTime(event))
                .build();
    }

    private String getFirstTimeSlotDateAndTime(Event event) {
        if (event.getTimeSlots() != null && !event.getTimeSlots().isEmpty()) {
            var firstSlot = event.getTimeSlots().get(0);
            return firstSlot.getDate() + " " + firstSlot.getStartTime();
        }
        return null;
    }

    @Override
    public ResponseEntity<?> eventAcceptAndReject(String eventId, String status) {
        AdminApprovalStatus approvalStatus = AdminApprovalStatus.PENDING;
        if (Objects.equals(status, "ACCEPTED")){
            approvalStatus = AdminApprovalStatus.ACCEPTED;
        } else if (Objects.equals(status, "REJECTED")) {
            approvalStatus = AdminApprovalStatus.REJECTED;
        }

        Optional<Event> eventOptional = eventRepository.findById(UUID.fromString(eventId));
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
        }

        Event event = eventOptional.get();

        if (event.getApprovalStatus() == approvalStatus) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Event is already in the requested approval state.");
        }

        if (approvalStatus != AdminApprovalStatus.PENDING){
            eventApprovalRequestRepository.deleteByEventId(UUID.fromString(eventId));
        }
        if (approvalStatus == AdminApprovalStatus.ACCEPTED){
            event.setEventState(EventState.PUBLISH);
        }
        event.setApprovalStatus(approvalStatus);
        eventRepository.save(event);

        String date = event.getTimeSlots().get(0).getDate();
        String startTime = event.getTimeSlots().get(0).getStartTime();
        String endTime = event.getTimeSlots().get(0).getEndTime();

        LocalDate localDate = LocalDate.parse(date);
        LocalTime localStartTime = LocalTime.parse(startTime);
        LocalTime localEndTime = LocalTime.parse(endTime);

        LocalDateTime startDateTime = LocalDateTime.of(localDate, localStartTime);
        LocalDateTime endDateTime = LocalDateTime.of(localDate, localEndTime);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startTimeAndDate = startDateTime.format(formatter);
        String endTimeAndDate = endDateTime.format(formatter);

        ScheduleEventDTO scheduleEventDTO = ScheduleEventDTO.builder()
                .imageUrl(event.getImageUrl())
                .eventId(eventId)
                .organizerId(event.getOrganizerId())
                .name(event.getName())
                .startDateAndTime(startTimeAndDate)
                .endDateAndTime(endTimeAndDate)
                .build();

        communicationClient.scheduleEvent(scheduleEventDTO);

        return ResponseEntity.ok("Event approval status updated successfully.");
    }

    @Override
    public ResponseEntity<?> eventBlockAndUnblock(String eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(UUID.fromString(eventId));

        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();

            event.setBlocked(!event.isBlocked());

            eventRepository.save(event);

            return ResponseEntity.ok("Event blocked status updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Event with ID " + eventId + " not found");
        }
    }


    @Override
    public ResponseEntity<?> getAllEventApprovals(int page, int size) {
        AdminApprovalStatus approvalStatus = AdminApprovalStatus.PENDING;

        Pageable pageable = PageRequest.of(page, size);

        List<EventApprovalRequests> approvalRequests = eventApprovalRequestRepository.findByApprovalStatus(approvalStatus, pageable);
        Set<UUID> eventIds = approvalRequests.stream()
                .map(EventApprovalRequests::getEventId)
                .collect(Collectors.toSet());

        List<Event> events = eventRepository.findAllById(eventIds);

        Map<UUID, Event> eventMap = events.stream()
                .collect(Collectors.toMap(Event::getId, event -> event));

        List<AllEventApprovalRequestResponse> approvalRequestResponses = approvalRequests.stream()
                .map(request -> {
                    Event event = eventMap.get(request.getEventId());
                    String eventName = event != null ? event.getName() : "Unknown Event";
                    boolean isOnline = event != null && event.isOnline();

                    return new AllEventApprovalRequestResponse(
                            request.getEventId().toString(),
                            eventName,
                            isOnline,
                            request.getApprovalStatus()
                    );
                }).collect(Collectors.toList());

        long totalElements = eventApprovalRequestRepository.countByApprovalStatus(approvalStatus);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        EventApprovalResponse response = new EventApprovalResponse(
                approvalRequestResponses,
                totalElements,
                totalPages,
                page,
                size
        );

        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<?> getOfflineEventDetail(String eventId, Long organizerId) {
        Optional<Event> eventOptional = eventRepository.findById(UUID.fromString(eventId));


        if (eventOptional.isEmpty() ||
                !eventOptional.get().getOrganizerId().equals(organizerId) ||
                eventOptional.get().isOnline()) {
            return ResponseEntity.notFound().build();
        }

        Event event = eventOptional.get();

        CreateOfflineEventDetailRequest eventDetailRequest = new CreateOfflineEventDetailRequest();
        eventDetailRequest.setId(event.getId().toString());
        eventDetailRequest.setName(event.getName());
        eventDetailRequest.setCategoryId(event.getCategoryId());
        eventDetailRequest.setDescription(event.getDescription());
        eventDetailRequest.setImageUrl(event.getImageUrl());
        eventDetailRequest.setLat(event.getLan());
        eventDetailRequest.setLng(event.getLon());
        eventDetailRequest.setAddress(event.getAddress());

        return ResponseEntity.ok(eventDetailRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<?> createOfflineEventTicket(String eventId, CreateOfflineEventTicketRequest offlineEventTicketRequest, Long organizerId) {

        log.info("Offline event request for event ID : {}", eventId);

        Optional<Event> eventOptional = eventRepository.findById(UUID.fromString(eventId));
        if (eventOptional.isEmpty()) {
            log.error("Event with ID {} not found", eventId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Event not found");
        }

        Event event = eventOptional.get();

        if (offlineEventTicketRequest.getTotalTickets() <= 0 || offlineEventTicketRequest.getCategories().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid offline event ticket data");
        }

        OfflineEventTickets offlineEventTickets = OfflineEventTickets.builder()
                .totalTickets(offlineEventTicketRequest.getTotalTickets())
                .categories(offlineEventTicketRequest.getCategories().stream()
                        .map(category -> new OfflineEventTickets.TicketCategory(
                                category.getName(),
                                Integer.valueOf(category.getTotalTickets()),
                                Double.valueOf(category.getPrice()),
                                category.getDiscountType(),
                                category.getDiscountValue() != null && !category.getDiscountValue().isEmpty() ?
                                        Double.valueOf(category.getDiscountValue()) : 0.0,
                                category.getDescription()))
                        .collect(Collectors.toList()))
                .build();

        event.setOfflineEventTickets(offlineEventTickets);

        Event updatedEvent = eventRepository.save(event);

        return ResponseEntity.status(HttpStatus.OK)
                .body("Offline tickets added successfully to event: " + updatedEvent.getId());
    }

    @Override
    public ResponseEntity<?> getOfflineEventTicket(String eventId, Long organizerId) {
        Optional<Event> eventOptional = eventRepository.findById(UUID.fromString(eventId));

        if (eventOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Event not found");
        }

        Event event = eventOptional.get();

        if (!event.getOrganizerId().equals(organizerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to view this event's tickets");
        }

        OfflineEventTickets offlineEventTickets = event.getOfflineEventTickets();
        if (offlineEventTickets == null || offlineEventTickets.getCategories().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No offline tickets found for this event");
        }

        CreateOfflineEventTicketRequest dto = convertToDTO(offlineEventTickets);

        return ResponseEntity.status(HttpStatus.OK)
                .body(dto);
    }

    private CreateOfflineEventTicketRequest convertToDTO(OfflineEventTickets offlineEventTickets) {
        List<CreateOfflineEventTicketRequest.TicketCategory> categories = offlineEventTickets.getCategories().stream()
                .map(category -> new CreateOfflineEventTicketRequest.TicketCategory(
                        category.getName(),
                        category.getTotalTickets().toString(),
                        category.getPrice().toString(),
                        category.getDiscountType(),
                        category.getDiscountValue().toString(),
                        category.getDescription()))
                .collect(Collectors.toList());

        return CreateOfflineEventTicketRequest.builder()
                .totalTickets(offlineEventTickets.getTotalTickets())
                .categories(categories)
                .build();
    }

    private String findCategoryById(String id) {
        Optional<EventCategory> category = eventCategoryRepository.findById(UUID.fromString(id));
        if (category.isPresent()) {
            return category.get().getName();
        }
        return null;
    }

    private OnlineEventSettingDTO convertToOnlineEventSettingDTO(OnlineEventSetting setting) {
        if (setting == null) {
            return null;
        }
        return OnlineEventSettingDTO.builder()
                .startImmediately(setting.isStartImmediately())
                .bookingStartDate(setting.getBookingStartDate())
                .bookingStartTime(setting.getBookingStartTime())
                .continueUntilEvent(setting.isContinueUntilEvent())
                .bookingEndDate(setting.getBookingEndDate())
                .bookingEndTime(setting.getBookingEndTime())
                .disableRefunds(setting.isDisableRefunds())
                .refundPercentage(setting.getRefundPercentage())
                .refundPolicy(setting.getRefundPolicy())
                .build();
    }


    private void updateEventFromRequest(Event event, CreateOfflineEventDetailRequest request, Long organizerId) {
        event.setName(request.getName());
        event.setCategoryId(request.getCategoryId());
        event.setDescription(request.getDescription());
        event.setImageUrl(request.getImageUrl());
        event.setOrganizerId(organizerId);
        event.setLan(request.getLat());
        event.setLon(request.getLng());
        event.setAddress(request.getAddress());
    }

    private OnlineEventTicketDTO convertToOnlineEventTicketDTO(OnlineEventTicket ticket) {
        if (ticket == null) {
            return null;
        }

        return OnlineEventTicketDTO.builder()
                .totalTickets(ticket.getTotalTickets() != null ? String.valueOf(ticket.getTotalTickets()) : null)
                .isFreeEvent(ticket.isFreeEvent())
                .price(ticket.getPrice() != null ? String.valueOf(ticket.getPrice()) : null)
                .hasEarlyBirdDiscount(ticket.isHasEarlyBirdDiscount())
                .discountType(ticket.getDiscountType())
                .discountValue(ticket.getDiscountValue() != null ? String.valueOf(ticket.getDiscountValue()) : null)
                .build();
    }

    private EventTimeSlotDTO convertToTimeSlotDTO(EventTimeSlot timeSlot) {
        return EventTimeSlotDTO.builder()
                .id(timeSlot.getId().toString())
                .date(timeSlot.getDate())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .duration(timeSlot.getDuration())
                .build();
    }

    private EventCategoryDTO convertToDTO(EventCategory eventCategory) {
        EventCategoryDTO dto = new EventCategoryDTO();
        dto.setId(eventCategory.getId());
        dto.setName(eventCategory.getName());
        dto.setDescription(eventCategory.getDescription());
        dto.setStatus(eventCategory.getStatus());
        return dto;
    }

}
