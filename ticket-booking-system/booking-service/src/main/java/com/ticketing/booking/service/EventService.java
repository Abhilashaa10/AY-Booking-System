package com.ticketing.booking.service;

import com.ticketing.booking.model.EventEntity;
import com.ticketing.booking.repository.EventRepository;
import com.ticketing.booking.repository.SeatRepository;
import com.ticketing.common.dto.EventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    
    public List<EventResponse> getAllEvents() {
        List<EventEntity> events = eventRepository.findAll();
        
        return events.stream().map(event -> {
            Long available = seatRepository.countByEventIdAndStatus(event.getId(), "AVAILABLE");
            
            return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .venue(event.getVenue())
                .eventDate(event.getEventDate())
                .totalSeats(event.getTotalSeats())
                .availableSeats(available)
                .priceRange("₹1,500 - ₹5,000") // Could be dynamic from seat min/max
                .imageUrl(event.getImageUrl())
                .build();
        }).collect(Collectors.toList());
    }
}