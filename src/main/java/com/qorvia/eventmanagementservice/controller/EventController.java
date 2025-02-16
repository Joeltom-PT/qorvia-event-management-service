package com.qorvia.eventmanagementservice.controller;

import com.qorvia.eventmanagementservice.dto.*;
import com.qorvia.eventmanagementservice.dto.request.CreateOfflineEventDetailRequest;
import com.qorvia.eventmanagementservice.dto.request.CreateOfflineEventTicketRequest;
import com.qorvia.eventmanagementservice.dto.request.CreateOnlineEventDetailRequest;
import com.qorvia.eventmanagementservice.dto.request.EventCategoryRequest;
import com.qorvia.eventmanagementservice.dto.response.ActiveEventCategoryDTO;
import com.qorvia.eventmanagementservice.dto.response.GetAllCategoriesResponse;
import com.qorvia.eventmanagementservice.security.RequireRole;
import com.qorvia.eventmanagementservice.security.RequireRoles;
import com.qorvia.eventmanagementservice.security.Roles;
import com.qorvia.eventmanagementservice.service.EventService;
import com.qorvia.eventmanagementservice.service.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    private final JwtService jwtService;


    @RequireRole(role = Roles.ORGANIZER)
    @PostMapping("/categoryRequest")
    public ResponseEntity<?> categoryRequest(@RequestBody EventCategoryRequest categoryRequest) {
        return eventService.categoryRequest(categoryRequest);
    }

    @RequireRole(role = Roles.ADMIN)
    @GetMapping("/getAllEventCategories")
    public ResponseEntity<GetAllCategoriesResponse> getAllEventCategories(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam("search") String search,
                                                                          @RequestParam("status") String status) {
        return eventService.getAllEventCategories(page, size, search, status);
    }

    @RequireRole(role = Roles.ADMIN)
    @PutMapping("/changeCategoryStatus/{id}")
    public ResponseEntity<?> changeCategoryStatus(@PathVariable("id") UUID id, @RequestParam String status) {
        return eventService.changeCategoryStatus(id, status);
    }

    @RequireRoles(roles = {Roles.ADMIN, Roles.ORGANIZER})
    @GetMapping("/getAllActiveEventCategories")
    public ResponseEntity<List<ActiveEventCategoryDTO>> getAllActiveEventCategories() {
        List<ActiveEventCategoryDTO> activeCategories = eventService.getAllActiveCategories();
        return ResponseEntity.ok(activeCategories);
    }

    // Online Event

    @RequireRole(role = Roles.ORGANIZER)
    @PostMapping("/createOnlineEvent/detail")
    public ResponseEntity<?> createOnlineEventDetail(
            @RequestBody CreateOnlineEventDetailRequest eventDetail,
            HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.createOnlineEventDetail(eventDetail, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @PutMapping("/createOnlineEvent/detail")
    public ResponseEntity<?> editOnlineEventDetail(
            @RequestBody CreateOnlineEventDetailRequest eventDetail,
            HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.createOnlineEventDetail(eventDetail, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @GetMapping("/getOnlineEvent/detail")
    public ResponseEntity<?> getOnlineEventDetail(@RequestParam String id,
                                                  HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getOnlineEventDetail(id, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @PutMapping("/createOnlineEvent/timeSlot/{id}")
    public ResponseEntity<?> createOnlineEventTimeSlot(
            @PathVariable("id") String eventId,
            @RequestBody List<EventTimeSlotDTO> timeSlotDTOs,
            HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.createOnlineEventTimeSlots(eventId, timeSlotDTOs, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @GetMapping("/getOnlineEvent/timeSlot/{id}")
    public ResponseEntity<?> getOnlineEventTimeSlots(@PathVariable String id, HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getOnlineEventTimeSlots(id, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @PutMapping("/createOnlineEvent/ticket/{id}")
    public ResponseEntity<?> createOnlineEventTicket(@PathVariable("id") String eventId,
                                                     @RequestBody OnlineEventTicketDTO onlineEventTicketDTO,
                                                     HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        log.info("Event is free while event register : {}", onlineEventTicketDTO.isFreeEvent());
        return eventService.createOnlineEventTicket(eventId, organizerId, onlineEventTicketDTO);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @GetMapping("/getOnlineEvent/ticket/{id}")
    public ResponseEntity<?> getOnlineEventTicket(@PathVariable("id") String eventId, HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getOnlineEventTicket(eventId, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @PutMapping("/createOnlineEvent/setting/{id}")
    public ResponseEntity<?> createOnlineEventSetting(@PathVariable("id") String eventId,
                                                      @RequestBody OnlineEventSettingDTO onlineEventSettingDto,
                                                      HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.createOnlineEventSettings(eventId, organizerId, onlineEventSettingDto);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @GetMapping("/getEvent/setting/{id}")
    public ResponseEntity<?> getEventSettings(@PathVariable("id") String eventId, HttpServletRequest servletRequest){
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getEventSettings(eventId,organizerId);
    }

    /// Offline event  APIs

    @RequireRole(role = Roles.ORGANIZER)
    @PostMapping("/createOfflineEvent/detail")
    public ResponseEntity<?> createOfflineEventDetail(@RequestBody CreateOfflineEventDetailRequest offlineEventDetailRequest,
                                                      HttpServletRequest servletRequest) {
        String eventId = offlineEventDetailRequest.getId();
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.createOfflineEventDetail(eventId, offlineEventDetailRequest, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @PutMapping("/createOfflineEvent/detail")
    public ResponseEntity<?> editOfflineEventDetail(@RequestBody CreateOfflineEventDetailRequest offlineEventDetailRequest,
                                                    HttpServletRequest servletRequest) {
        String eventId = offlineEventDetailRequest.getId();
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.createOfflineEventDetail(eventId, offlineEventDetailRequest, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @GetMapping("/getOfflineEvent/detail")
    public ResponseEntity<?> getOfflineEventDetail(@RequestParam("id") String eventId,
                                                   HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getOfflineEventDetail(eventId, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @PutMapping("/createOfflineEvent/ticket/{id}")
    public ResponseEntity<?> createOfflineEventTicket(@PathVariable("id") String eventId,
                                                      @RequestBody CreateOfflineEventTicketRequest offlineEventTicketRequest,
                                                      HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.createOfflineEventTicket(eventId, offlineEventTicketRequest, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @GetMapping("/getOfflineEvent/ticket/{id}")
    public ResponseEntity<?> getOfflineEventTicket(@PathVariable("id") String eventId,
                                                   HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getOfflineEventTicket(eventId, organizerId);
    }

    // Event Management OrganizerSide
    @RequireRole(role = Roles.ORGANIZER)
    @GetMapping("/getEventsByOrganizer")
    public ResponseEntity<List<OrganizerEventListingPageResponse>> getEventsByOrganizer(HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        List<OrganizerEventListingPageResponse> events = eventService.getEventsByOrganizer(organizerId);
        return ResponseEntity.ok(events);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @GetMapping("/getSpecificEventById/{id}")
    public ResponseEntity<OrganizerEventListingPageResponse> getSpecificEventById(@PathVariable("id") String eventId,
                                                                                  HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getSpecificEventByOrganizerId(eventId, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @PostMapping("/request-admin-approval/{id}")
    public ResponseEntity<?> requestAdminApprovalForEvent(@PathVariable("id") String eventId,
                                                          HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.requestAdminApprovalForEvent(eventId, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @PutMapping("/deleteEvent/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable("id") String eventId,
                                         HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.deleteEvent(eventId, organizerId);
    }

    @RequireRole(role = Roles.ORGANIZER)
    @PutMapping("/withdraw-event-approval-request/{id}")
    public ResponseEntity<?> withdrawEventApprovalRequest(@PathVariable("id") String eventId,
                                                          HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.withdrawEventApprovalRequest(eventId, organizerId);
    }


    @RequireRole(role = Roles.ADMIN)
    @GetMapping("/getAllEventApprovalRequest")
    public ResponseEntity<?> getAllEventApprovalRequest(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        log.info("Request for get all event approval status ......./////////////");
        return eventService.getAllEventApprovals(page, size);
    }

    @RequireRole(role = Roles.ADMIN)
    @GetMapping("/getEventDetails/{id}")
    public ResponseEntity<?> getEventDetails(@PathVariable("id") String eventId) {
        return eventService.getEventDetails(eventId);
    }

    @RequireRole(role = Roles.ADMIN)
    @PutMapping("/eventAcceptAndReject/{id}")
    public ResponseEntity<?> eventAcceptAndReject(
            @PathVariable("id") String eventId,
            @RequestBody Map<String, String> requestBody) {
        String status = requestBody.get("status");
        return eventService.eventAcceptAndReject(eventId, status);
    }

    @RequireRole(role = Roles.ADMIN)
    @PutMapping("/eventBlockAndUnblock/{id}")
    public ResponseEntity<?> eventBlockAndUnblock(@PathVariable("id") String eventId) {
        return eventService.eventBlockAndUnblock(eventId);
    }

    @RequireRole(role = Roles.ADMIN)
    @GetMapping("/getAllApprovedEvents")
    public ResponseEntity<?> getAllApprovedEvents(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "eventState", required = false) String eventState,
            @RequestParam(name = "isOnline", required = false) Boolean isOnline,
            @RequestParam(name = "categoryId", required = false) String categoryId) {

        return eventService.getAllApprovedEvents(page, size, search, eventState, isOnline, categoryId);
    }


    @GetMapping("/getAllEvents")
    public ResponseEntity<?> getAllEvents(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "8") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "isOnline" , required = false) Boolean isOnline,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "organizerId", required = false) String organizerId,
            @RequestParam(name = "date", required = false) String date){

            return eventService.getAllEvents(page, size, search, isOnline, categoryId, organizerId, date);
    }

    @GetMapping("/getOnlineEventData/{id}")
    public ResponseEntity<?> getOnlineEventData(@PathVariable("id") String eventId, HttpServletRequest servletRequest){
        Long userId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getOnlineEventData(eventId, userId);
    }

    @GetMapping("/getOfflineEventData/{id}")
    public ResponseEntity<?> getOfflineEventData(@PathVariable("id") String eventId, HttpServletRequest servletRequest){
        Long userId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getOfflineEventData(eventId, userId);
    }


    @GetMapping("/getAllRegisteredEvents")
    @RequireRole(role = Roles.USER)
    public ResponseEntity<List<RegisteredEventDTO>> getAllRegisteredEvents(HttpServletRequest servletRequest){
        Long userId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getAllRegisteredEventsByUserId(userId);
    }

    @GetMapping("/fetch-all-live")
    @RequireRole(role = Roles.ORGANIZER)
    public ResponseEntity<List<LiveEvent>> getAllLive(HttpServletRequest servletRequest){
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getAllLive(organizerId);
    }

    @GetMapping("/featured-events/{id}/{count}")
    public ResponseEntity<?> getFeaturedEvents(@PathVariable("id") String eventId,@PathVariable("count") int count){
        log.info("Featured event finding with event id : {}, and count : {}", eventId, count);
        return eventService.getFeaturedEvents(eventId,count);
    }

    @GetMapping("/get-user-calender")
    @RequireRole(role = Roles.USER)
    public ResponseEntity<?> getUserCalendar(HttpServletRequest servletRequest){
        long userId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getUserCalendar(userId);
    }

    @GetMapping("/get-organizer-calender")
    @RequireRole(role = Roles.ORGANIZER)
    public ResponseEntity<?> getOrganizerCalender(HttpServletRequest httpServletRequest){
        long organizerId = jwtService.getUserIdFormRequest(httpServletRequest);
        return eventService.getOrganizerCalender(organizerId);
    }

   @GetMapping("/get-profile-events/{id}")
   public ResponseEntity<?> getProfileEvents(@PathVariable("id") Long organizerId){
     return eventService.getProfileEvents(organizerId);
   }

    @GetMapping("/getByIds")
    public ResponseEntity<?> getDataByIds(@RequestParam List<String> ids) {
        try {
            List<GetEventInfoDTO> events = eventService.getEventsByIds(ids);

            if (events.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No events found for the provided IDs.");
            }

            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching events.");
        }
    }

    @GetMapping("/statistics")
    @RequireRole(role = Roles.ORGANIZER)
    public ResponseEntity<?> getEventStatistics(HttpServletRequest servletRequest){
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return eventService.getEventStatistics(organizerId);
    }


}
