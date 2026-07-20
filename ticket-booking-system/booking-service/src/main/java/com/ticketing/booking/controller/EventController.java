// package com.ticketing.booking.controller;

// import com.ticketing.booking.service.EventService;
// import com.ticketing.common.dto.EventResponse;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import java.util.List;

// @RestController
// @RequestMapping("/api/v1/events")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "*")
// public class EventController {
    
//     private final EventService eventService;
    
//     @GetMapping
//     public ResponseEntity<List<EventResponse>> getAllEvents() {
//         return ResponseEntity.ok(eventService.getAllEvents());
//     }
// }

package com.ticketing.booking.controller;

import com.ticketing.booking.service.EventService;
import com.ticketing.common.dto.EventDetailResponse;
import com.ticketing.common.dto.EventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EventController {
    
    private final EventService eventService;
    
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        log.info("GET /api/v1/events - Fetching all events");
        return ResponseEntity.ok(eventService.getAllEvents());
    }
    
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID eventId) {
        log.info("GET /api/v1/events/{} - Fetching event by id", eventId);
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }
    
    @GetMapping("/{eventId}/detail")
    public ResponseEntity<EventDetailResponse> getEventDetail(@PathVariable UUID eventId) {
        log.info("GET /api/v1/events/{}/detail - Fetching event detail", eventId);
        return ResponseEntity.ok(eventService.getEventDetail(eventId));
    }
}