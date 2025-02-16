package com.qorvia.eventmanagementservice.dto.response;

import com.qorvia.eventmanagementservice.model.AdminApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllEventApprovalRequestResponse {
    private String eventId;
    private String eventName;
    private boolean isOnline;
    private AdminApprovalStatus approvalStatus;

}
