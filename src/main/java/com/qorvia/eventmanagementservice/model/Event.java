package com.qorvia.eventmanagementservice.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "events")
public class Event {

    @Id
    private UUID id;

    private Long organizerId;

    private EventState eventState;

    private String name;

    private String categoryId;

    private EventType eventType;

    private boolean isOnline;

    private boolean isBlocked;

    private String description;

    private String imageUrl;

    private String address;

    private Double lan;

    private Double lon;

    private EventFormStatus eventFormStatus;

    private AdminApprovalStatus approvalStatus;

    private boolean isDeleted;

    @CreatedDate
    private String createdAt;

    @LastModifiedDate
    private String updatedAt;

    private List<EventTimeSlot> timeSlots = new ArrayList<>();

    private OnlineEventTicket onlineEventTicket;

    private OfflineEventTickets offlineEventTickets;

    private OnlineEventSetting onlineEventSetting;

}
