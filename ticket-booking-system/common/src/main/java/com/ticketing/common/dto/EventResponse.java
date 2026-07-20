package com.ticketing.common.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventResponse {
    private UUID id;
    private String name;
    private String venue;
    private LocalDateTime eventDate;
    private Integer totalSeats;
    private Long availableSeats;
    private String priceRange;
    private String imageUrl;
}