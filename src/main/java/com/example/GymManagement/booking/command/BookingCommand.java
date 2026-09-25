package com.example.GymManagement.booking.command;

import com.example.GymManagement.booking.dto.BookingResultDto;

public interface BookingCommand {
    BookingResultDto execute();
    String describe();
}