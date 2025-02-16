package com.qorvia.eventmanagementservice.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnlineEventSetting {

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
