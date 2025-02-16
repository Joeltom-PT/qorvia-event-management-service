package com.qorvia.eventmanagementservice.dto.response;

import com.qorvia.eventmanagementservice.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedUsersListResponse {
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
    private List<BookedUsers> bookedUsers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookedUsers {
        private Long userId;
        private String userName;
        private String email;
        private String address;
        private String country;
        private String state;
        private String city;
        private String zipCode;
        private List<Booking.Ticket> tickets;
    }

}
