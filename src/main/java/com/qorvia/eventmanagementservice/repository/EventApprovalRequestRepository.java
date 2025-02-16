package com.qorvia.eventmanagementservice.repository;


import com.qorvia.eventmanagementservice.model.AdminApprovalStatus;
import com.qorvia.eventmanagementservice.model.EventApprovalRequests;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface EventApprovalRequestRepository extends MongoRepository<EventApprovalRequests, UUID> {

    void deleteByEventId(UUID eventId);

    List<EventApprovalRequests> findByApprovalStatus(AdminApprovalStatus approvalStatus, Pageable pageable);


    long countByApprovalStatus(AdminApprovalStatus approvalStatus);
}
