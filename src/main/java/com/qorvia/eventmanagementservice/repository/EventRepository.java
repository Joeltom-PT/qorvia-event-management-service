package com.qorvia.eventmanagementservice.repository;


import com.qorvia.eventmanagementservice.model.AdminApprovalStatus;
import com.qorvia.eventmanagementservice.model.Event;
import com.qorvia.eventmanagementservice.repository.custom.CustomEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends MongoRepository<Event, UUID>, CustomEventRepository {
    List<Event> findByOrganizerIdAndIsDeletedFalse(Long organizerId);

    Optional<Event> findByIdAndOrganizerId(UUID eventId, Long organizerId);

    List<Event> findByOrganizerId(Long organizerId);
}
