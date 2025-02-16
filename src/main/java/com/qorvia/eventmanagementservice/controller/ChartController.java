package com.qorvia.eventmanagementservice.controller;

import com.qorvia.eventmanagementservice.security.RequireRole;
import com.qorvia.eventmanagementservice.security.Roles;
import com.qorvia.eventmanagementservice.service.ChartService;
import com.qorvia.eventmanagementservice.service.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event/chart")
@RequiredArgsConstructor
@Slf4j
public class ChartController {

    private final JwtService jwtService;
    private final ChartService chartService;

    @GetMapping("/bookings/{type}/{eventId}")
    @RequireRole(role = Roles.ORGANIZER)
    public ResponseEntity<?> getChartDataByBookings(
            @PathVariable("type") String type,
            @PathVariable("eventId") String eventId,
            HttpServletRequest servletRequest) {
        Long organizerId = jwtService.getUserIdFormRequest(servletRequest);
        return chartService.getChartDataByBookings(organizerId, eventId, type);
    }
}
