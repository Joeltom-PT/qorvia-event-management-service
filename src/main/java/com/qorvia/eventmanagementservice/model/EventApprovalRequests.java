package com.qorvia.eventmanagementservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "event_approval_status")
public class EventApprovalRequests {
    @Id
    private UUID id;

    private UUID eventId;

    private AdminApprovalStatus approvalStatus;
}
