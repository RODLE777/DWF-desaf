package sv.udb.edu.dto.response;

import sv.udb.edu.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Integer idBooking;
    private Integer eventId;
    private String eventTitle;
    private String eventVenue;
    private LocalDateTime eventDate;
    private String username;
    private Integer quantity;
    private BigDecimal totalAmount;
    private LocalDateTime bookingDate;
    private BookingStatus status;
}