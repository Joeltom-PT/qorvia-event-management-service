package com.qorvia.eventmanagementservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventApprovalResponse {

    private List<AllEventApprovalRequestResponse> eventApproval;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
}
