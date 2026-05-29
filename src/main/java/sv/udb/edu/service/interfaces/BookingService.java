package sv.udb.edu.service.interfaces;

import sv.udb.edu.dto.request.BookingRequest;
import sv.udb.edu.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, String username);
    List<BookingResponse> getMyBookings(String username);
    BookingResponse cancelBooking(Integer bookingId, String username);
}