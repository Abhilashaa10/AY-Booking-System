package com.ticketing.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDetailResponse {
    
    private UUID id;
    private String name;
    private String venue;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime eventDate;
    
    private Integer totalSeats;
    private Long availableSeats;
    private String priceRange;
    
    private List<SectionInfo> sections;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectionInfo {
        private String name;
        private Integer seatCount;
        private Double price;
        private Long availableSeats;
    }
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}