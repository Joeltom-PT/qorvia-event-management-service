package com.qorvia.eventmanagementservice.service;

import com.qorvia.eventmanagementservice.dto.*;
import com.qorvia.eventmanagementservice.dto.request.CreateOfflineEventDetailRequest;
import com.qorvia.eventmanagementservice.dto.request.CreateOfflineEventTicketRequest;
import com.qorvia.eventmanagementservice.dto.request.CreateOnlineEventDetailRequest;
import com.qorvia.eventmanagementservice.dto.request.EventCategoryRequest;
import com.qorvia.eventmanagementservice.dto.response.ActiveEventCategoryDTO;
import com.qorvia.eventmanagementservice.dto.response.GetAllCategoriesResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface EventService {
    ResponseEntity<?> categoryRequest(EventCategoryRequest categoryRequest);

    ResponseEntity<?> changeCategoryStatus(UUID id, String status);

    ResponseEntity<GetAllCategoriesResponse> getAllEventCategories(int page, int size, String search, String status);

    ResponseEntity<?> createOnlineEventDetail(CreateOnlineEventDetailRequest eventDetail, Long organizerId);

    List<ActiveEventCategoryDTO> getAllActiveCategories();

    ResponseEntity<?> getOnlineEventDetail(String id, Long organizerId);

    ResponseEntity<?> createOnlineEventTimeSlots(String eventId, List<EventTimeSlotDTO> timeSlotDTOs, Long organizerId);

    ResponseEntity<?> getOnlineEventTimeSlots(String id, Long organizerId);

    ResponseEntity<?> createOnlineEventTicket(String eventId, Long organizerId, OnlineEventTicketDTO onlineEventTicketDTO);

    ResponseEntity<?> getOnlineEventTicket(String eventId, Long organizerId);

    ResponseEntity<?> createOnlineEventSettings(String eventId, Long organizerId, OnlineEventSettingDTO onlineEventSettingDto);

    ResponseEntity<?> createOfflineEventDetail(String eventId, CreateOfflineEventDetailRequest offlineEventDetailRequest, Long organizerId);


    ResponseEntity<?> getOfflineEventDetail(String eventId, Long organizerId);

    ResponseEntity<?> createOfflineEventTicket(String eventId, CreateOfflineEventTicketRequest offlineEventTicketRequest, Long organizerId);

    ResponseEntity<?> getOfflineEventTicket(String eventId, Long organizerId);

    List<OrganizerEventListingPageResponse> getEventsByOrganizer(Long organizerId);

    ResponseEntity<OrganizerEventListingPageResponse> getSpecificEventByOrganizerId(String eventId, Long organizerId);

    ResponseEntity<?> requestAdminApprovalForEvent(String eventId, Long organizerId);

    ResponseEntity<?> getAllEventApprovals(int page, int size);

    ResponseEntity<?> deleteEvent(String eventId, Long organizerId);

    ResponseEntity<?> withdrawEventApprovalRequest(String eventId, Long organizerId);

    ResponseEntity<?> getEventDetails(String eventId);


    ResponseEntity<?> eventAcceptAndReject(String eventId, String status);

    ResponseEntity<?> eventBlockAndUnblock(String eventId);

    ResponseEntity<?> getAllApprovedEvents(int page, int size, String search, String eventState, Boolean isOnline, String categoryId);

    ResponseEntity<?> getAllEvents(int page, int size, String search, Boolean isOnline, String categoryId, String organizerId, String date);

    ResponseEntity<?> getOnlineEventData(String eventId, Long userId);

    ResponseEntity<?> getOfflineEventData(String eventId, Long userId);

    ResponseEntity<?> getEventSettings(String eventId, Long organizerId);

    ResponseEntity<List<RegisteredEventDTO>> getAllRegisteredEventsByUserId(Long userId);

    ResponseEntity<List<LiveEvent>> getAllLive(Long organizerId);

    ResponseEntity<?> getFeaturedEvents(String eventId, int count);

    ResponseEntity<?> getUserCalendar(long userId);

    ResponseEntity<?> getOrganizerCalender(long organizerId);

    ResponseEntity<?> getProfileEvents(Long organizerId);

    List<GetEventInfoDTO> getEventsByIds(List<String> ids);

    ResponseEntity<?> getEventStatistics(Long organizerId);
}
