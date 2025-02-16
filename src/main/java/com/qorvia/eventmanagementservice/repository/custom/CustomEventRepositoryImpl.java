package com.qorvia.eventmanagementservice.repository.custom;

import com.qorvia.eventmanagementservice.dto.CalendarDataDTO;
import com.qorvia.eventmanagementservice.model.AdminApprovalStatus;
import com.qorvia.eventmanagementservice.model.Event;
import com.qorvia.eventmanagementservice.model.EventState;
import com.qorvia.eventmanagementservice.model.EventTimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class CustomEventRepositoryImpl implements CustomEventRepository {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public List<CalendarDataDTO> getOrganizerCalender(long organizerId) {
        Criteria criteria = Criteria.where("organizerId").is(organizerId)
                .and("approvalStatus").is("ACCEPTED")
                .and("eventState").is(EventState.PUBLISH)
                .and("isBlocked").is(false)
                .and("isDeleted").is(false);

        List<Event> events = mongoTemplate.find(Query.query(criteria), Event.class);

        return events.stream()
                .flatMap(event -> event.getTimeSlots().stream()
                        .map(timeSlot -> CalendarDataDTO.builder()
                                .eventId(event.getId().toString())
                                .name(event.getName())
                                .startTimeAndDate(getStartTimeAndDate(timeSlot))
                                .endTimeAndDate(getEndTimeAndDate(timeSlot))
                                .imageUrl(event.getImageUrl())
                                .isOnline(event.isOnline())
                                .build()))
                .collect(Collectors.toList());
    }

    private String getStartTimeAndDate(EventTimeSlot timeSlot) {
        LocalDateTime startDateTime = LocalDateTime.parse(timeSlot.getDate() + " " + timeSlot.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
    }

    private String getEndTimeAndDate(EventTimeSlot timeSlot) {
        LocalDateTime endDateTime = LocalDateTime.parse(timeSlot.getDate() + " " + timeSlot.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
    }

    @Override
    public Page<Event> findAllApprovedEvents(String eventState, Boolean isOnline, String categoryId,
                                             AdminApprovalStatus approvalStatus, String search, Pageable pageable) {

        Query query = new Query();

        if (eventState != null) {
            query.addCriteria(Criteria.where("eventState").is(eventState));
        }

        if (categoryId != null) {
            query.addCriteria(Criteria.where("categoryId").is(categoryId));
        }

        if (approvalStatus != null) {
            query.addCriteria(Criteria.where("approvalStatus").is(approvalStatus));
        }

        if (isOnline != null) {
            query.addCriteria(Criteria.where("isOnline").is(isOnline));
        }

        if (search != null && !search.trim().isEmpty()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("name").regex(search, "i"),
                    Criteria.where("description").regex(search, "i")
            ));
        }

        query.with(pageable);

        List<Event> events = mongoTemplate.find(query, Event.class);
        long total = mongoTemplate.count(query, Event.class);

        return new PageImpl<>(events, pageable, total);
    }


    @Override
    public Page<Event> findAllEvents(String search, Boolean isOnline, String categoryId, String organizerId, String date, Pageable pageable) {
        Query query = new Query();

        query.addCriteria(Criteria.where("approvalStatus").is(AdminApprovalStatus.ACCEPTED));
        query.addCriteria(Criteria.where("isDeleted").is(false));
        query.addCriteria(Criteria.where("isBlocked").is(false));
        query.addCriteria(Criteria.where("eventState").is(EventState.PUBLISH));

        if (organizerId != null && !organizerId.trim().isEmpty()) {
            query.addCriteria(Criteria.where("organizerId").is(organizerId));
        }

        if (categoryId != null && !categoryId.trim().isEmpty()) {
            query.addCriteria(Criteria.where("categoryId").is(categoryId));
        }

        if (isOnline != null) {
            query.addCriteria(Criteria.where("isOnline").is(isOnline));
        }

        if (date != null && !date.trim().isEmpty()) {
            query.addCriteria(Criteria.where("timeSlots.date").is(date));
        }

        if (search != null && !search.trim().isEmpty()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("name").regex(search, "i"),
                    Criteria.where("description").regex(search, "i")
            ));
        }

        query.with(pageable);

        List<Event> events = mongoTemplate.find(query, Event.class);
        long total = mongoTemplate.count(query, Event.class);

        return new PageImpl<>(events, pageable, total);
    }


    @Override
    public List<Event> getFeaturedEvents(UUID eventId, int count) {
        List<Event> result = new ArrayList<>();

        Event baseEvent = mongoTemplate.findById(eventId, Event.class);

        if (baseEvent != null) {
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").ne(eventId));
            query.addCriteria(Criteria.where("categoryId").is(baseEvent.getCategoryId()));
            query.addCriteria(Criteria.where("organizerId").is(baseEvent.getOrganizerId()));
            query.addCriteria(Criteria.where("eventState").is(EventState.PUBLISH));
            query.addCriteria(Criteria.where("isDeleted").is(false));
            query.addCriteria(Criteria.where("isBlocked").is(false));
            query.addCriteria(Criteria.where("approvalStatus").is(AdminApprovalStatus.ACCEPTED));
            query.limit(count);

            result = mongoTemplate.find(query, Event.class);
        }

        if (result.size() < count) {
            int remaining = count - result.size();
            Query randomQuery = new Query();

            List<UUID> excludeIds = new ArrayList<>();
            excludeIds.add(eventId);
            if (!result.isEmpty()) {
                excludeIds.addAll(result.stream().map(Event::getId).toList());
            }

            randomQuery.addCriteria(Criteria.where("_id").nin(excludeIds));
            randomQuery.addCriteria(Criteria.where("eventState").is(EventState.PUBLISH));
            randomQuery.addCriteria(Criteria.where("isDeleted").is(false));
            randomQuery.addCriteria(Criteria.where("isBlocked").is(false));
            randomQuery.addCriteria(Criteria.where("approvalStatus").is(AdminApprovalStatus.ACCEPTED));
            randomQuery.limit(remaining);

            List<Event> randomEvents = mongoTemplate.find(randomQuery, Event.class);
            result.addAll(randomEvents);
        }

        return result;
    }

    @Override
    public List<Event> findTop5FilteredEventsByOrganizer(Long organizerId) {
        Query query = new Query();

        query.addCriteria(Criteria.where("isBlocked").is(false));
        query.addCriteria(Criteria.where("isDeleted").is(false));
        query.addCriteria(Criteria.where("eventState").is(EventState.PUBLISH));
        query.addCriteria(Criteria.where("approvalStatus").is(AdminApprovalStatus.ACCEPTED));
        query.addCriteria(Criteria.where("organizerId").is(organizerId));

        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.limit(5);

        return mongoTemplate.find(query, Event.class);
    }

}
