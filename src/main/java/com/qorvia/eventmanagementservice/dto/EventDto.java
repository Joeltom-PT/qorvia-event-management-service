package com.qorvia.eventmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {

    private UUID id;

    private Long organizerId;

    private String eventState;

    private String name;

    private String categoryId;

    private String eventType;

    @JsonProperty("isOnline")
    private boolean isOnline;

    @JsonProperty("isBlocked")
    private boolean isBlocked;

    private String description;

    private String imageUrl;

    private String address;

    private Double lan;

    private Double lon;

    private String eventFormStatus;

    private String approvalStatus;

    @JsonProperty("isDeleted")
    private boolean isDeleted;

    private String createdAt;

    private String updatedAt;

    private List<EventTimeSlotDto> timeSlots;

    private OnlineEventTicketDto onlineEventTicket;

    private OfflineEventTicketsDto offlineEventTickets;

    private EventSettingDto EventSettingDto;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventTimeSlotDto {
        private UUID id;
        private UUID eventId;
        private String date;
        private String startTime;
        private String endTime;
        private String duration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnlineEventTicketDto {
        private Integer totalTickets;
        @JsonProperty("isFreeEvent")
        private boolean isFreeEvent;
        private Integer price;
        private boolean hasEarlyBirdDiscount;
        private String discountType;
        private Integer discountValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfflineEventTicketsDto {
        private Integer totalTickets;
        private List<TicketCategoryDto> categories;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TicketCategoryDto {
            private String name;
            private Integer totalTickets;
            private Double price;
            private String discountType;
            private Double discountValue;
            private String description;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSettingDto {
        private boolean startImmediately;
        private String bookingStartDate;
        private String bookingStartTime;
        private boolean continueUntilEvent;
        private String bookingEndDate;
        private String bookingEndTime;
        private boolean disableRefunds;
        private String refundPercentage;
        private String refundPolicy;
    }
}
