package com.qorvia.eventmanagementservice.dto.response;

import com.qorvia.eventmanagementservice.dto.EventCategoryDTO;
import lombok.Data;

import java.util.List;

@Data
public class GetAllCategoriesResponse {
    List<EventCategoryDTO> categories;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
}
