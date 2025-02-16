package com.qorvia.eventmanagementservice.repository.custom;

import com.qorvia.eventmanagementservice.dto.CalendarDataDTO;
import com.qorvia.eventmanagementservice.model.AdminApprovalStatus;
import com.qorvia.eventmanagementservice.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomEventRepository {
    Page<Event> findAllApprovedEvents(String eventState, Boolean isOnline, String categoryId,
                                      AdminApprovalStatus approvalStatus, String search, Pageable pageable);

    Page<Event> findAllEvents(String search, Boolean isOnline, String categoryId, String organizerId, String date, Pageable pageable);

    public List<Event> getFeaturedEvents(UUID eventId, int count);

    public List<CalendarDataDTO> getOrganizerCalender(long organizerId);

    public List<Event> findTop5FilteredEventsByOrganizer(Long organizerId);

}
