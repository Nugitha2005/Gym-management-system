package com.example.GymManagement.booking.command;

import com.example.GymManagement.booking.dto.BookingResultDto;
import org.springframework.stereotype.Component;

@Component
public class BookingCommandInvoker {
    public BookingResultDto executeCommand(BookingCommand command) {
        return command.execute();
    }
}