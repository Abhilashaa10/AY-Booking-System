// package com.ticketing.booking.service;

// import com.ticketing.booking.model.EventEntity;
// import com.ticketing.booking.repository.EventRepository;
// import com.ticketing.booking.repository.SeatRepository;
// import com.ticketing.common.dto.EventResponse;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// public class EventService {
    
//     private final EventRepository eventRepository;
//     private final SeatRepository seatRepository;
    
//     public List<EventResponse> getAllEvents() {
//         List<EventEntity> events = eventRepository.findAll();
        
//         return events.stream().map(event -> {
//             Long available = seatRepository.countByEventIdAndStatus(event.getId(), "AVAILABLE");
            
//             return EventResponse.builder()
//                 .id(event.getId())
//                 .name(event.getName())
//                 .venue(event.getVenue())
//                 .eventDate(event.getEventDate())
//                 .totalSeats(event.getTotalSeats())
//                 .availableSeats(available)
//                 .priceRange("₹1,500 - ₹5,000") // Could be dynamic from seat min/max
//                 .imageUrl(event.getImageUrl())
//                 .build();
//         }).collect(Collectors.toList());
//     }
// }

package com.ticketing.booking.service;

import com.ticketing.booking.model.EventEntity;
import com.ticketing.booking.model.SeatEntity;
import com.ticketing.booking.model.SeatEntity.SeatStatus;
import com.ticketing.booking.repository.EventRepository;
import com.ticketing.booking.repository.SeatRepository;
import com.ticketing.common.dto.EventDetailResponse;
import com.ticketing.common.dto.EventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        log.info("Fetching all events");
        List<EventEntity> events = eventRepository.findAll();
        return events.stream()
            .map(this::mapToEventResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID eventId) {
        log.info("Fetching event by id: {}", eventId);
        EventEntity event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        return mapToEventResponse(event);
    }
    
    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(UUID eventId) {
        log.info("Fetching event detail for id: {}", eventId);
        
        EventEntity event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        List<SeatEntity> seats = seatRepository.findByEventId(eventId);
        
        Map<String, List<SeatEntity>> seatsBySection = seats.stream()
            .collect(Collectors.groupingBy(SeatEntity::getSection));
        
        List<EventDetailResponse.SectionInfo> sections = seatsBySection.entrySet().stream()
            .map(entry -> {
                String sectionName = entry.getKey();
                List<SeatEntity> sectionSeats = entry.getValue();
                
                long availableInSection = sectionSeats.stream()
                    .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                    .count();
                
                BigDecimal price = sectionSeats.isEmpty() ? BigDecimal.ZERO : sectionSeats.get(0).getPrice();
                
                return EventDetailResponse.SectionInfo.builder()
                    .name(sectionName)
                    .seatCount(sectionSeats.size())
                    .price(price.doubleValue())
                    .availableSeats(availableInSection)
                    .build();
            })
            .sorted(Comparator.comparing(s -> {
                switch (s.getName()) {
                    case "VIP": return 1;
                    case "PREMIUM": return 2;
                    default: return 3;
                }
            }))
            .collect(Collectors.toList());
        
        Long totalAvailable = eventRepository.countAvailableSeatsByEventId(eventId);
        BigDecimal minPrice = eventRepository.findMinPriceByEventId(eventId);
        BigDecimal maxPrice = eventRepository.findMaxPriceByEventId(eventId);
        
        return EventDetailResponse.builder()
            .id(event.getId())
            .name(event.getName())
            .venue(event.getVenue())
            .eventDate(event.getEventDate())
            .totalSeats(event.getTotalSeats())
            .availableSeats(totalAvailable != null ? totalAvailable : 0L)
            .priceRange(formatPriceRange(minPrice, maxPrice))
            .sections(sections)
            .createdAt(event.getCreatedAt())
            .build();
    }
    
    private EventResponse mapToEventResponse(EventEntity event) {
        Long availableSeats = eventRepository.countAvailableSeatsByEventId(event.getId());
        BigDecimal minPrice = eventRepository.findMinPriceByEventId(event.getId());
        BigDecimal maxPrice = eventRepository.findMaxPriceByEventId(event.getId());
        
        return EventResponse.builder()
            .id(event.getId())
            .name(event.getName())
            .venue(event.getVenue())
            .eventDate(event.getEventDate())
            .totalSeats(event.getTotalSeats())
            .availableSeats(availableSeats != null ? availableSeats : 0L)
            .priceRange(formatPriceRange(minPrice, maxPrice))
            .createdAt(event.getCreatedAt())
            .build();
    }
    
    private String formatPriceRange(BigDecimal min, BigDecimal max) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        if (min == null || max == null) {
            return "Price not available";
        }
        if (min.compareTo(max) == 0) {
            return currencyFormat.format(min);
        }
        return currencyFormat.format(min) + " - " + currencyFormat.format(max);
    }
}