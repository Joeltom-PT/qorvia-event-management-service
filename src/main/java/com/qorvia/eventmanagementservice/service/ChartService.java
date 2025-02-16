package com.qorvia.eventmanagementservice.service;

import org.springframework.http.ResponseEntity;

public interface ChartService {
    ResponseEntity<?> getChartDataByBookings(Long organizerId, String eventId, String type);
}
