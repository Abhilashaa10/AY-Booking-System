package com.ticketing.booking.exception;

public class SeatAlreadyBookedException extends RuntimeException {
    public SeatAlreadyBookedException(String seatId) {
        super("Seat " + seatId + " is already held or booked by another user.");
    }
}