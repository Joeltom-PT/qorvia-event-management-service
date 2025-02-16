package com.qorvia.eventmanagementservice.dto;

import com.qorvia.eventmanagementservice.dto.EventTimeSlotDTO;
import com.qorvia.eventmanagementservice.dto.OfflineEventTicketsDTO;
import com.qorvia.eventmanagementservice.dto.OnlineEventSettingDTO;
import com.qorvia.eventmanagementservice.dto.OnlineEventTicketDTO;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerEventListingPageResponse {

    private String id;
    private Long organizerId;
    private String eventState;
    private String name;
    private String category;
    private String eventType;
    private String approvalStatus;
    @JsonProperty("isOnline")
    private boolean isOnline;

    private String price;

    private String imageUrl;

    private String eventFormStatus;

    private Integer totalTickets;

    @JsonProperty("isDeleted")
    private boolean isDeleted;

    private String createdAt;

    private String updatedAt;

    private String startDate;

    private String startDateAndTime;

    private String endDateAndTime;

    private String duration;

    @JsonProperty("isOnline")
    public boolean getIsOnline() {
        return isOnline;
    }

    @JsonProperty("isOnline")
    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    @JsonProperty("isDeleted")
    public boolean getIsDeleted() {
        return isDeleted;
    }

    @JsonProperty("isDeleted")
    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}




