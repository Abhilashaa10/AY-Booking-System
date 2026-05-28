package com.ticketing.payment.controller;

import com.ticketing.payment.model.PaymentEntity;
import com.ticketing.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * GET /api/v1/payments/booking/{bookingId}
     * Returns payment status for a given booking.
     * Used by frontend to poll payment result.
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentEntity> getPaymentStatus(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentByBookingId(bookingId));
    }
}