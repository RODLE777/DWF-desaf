package sv.udb.edu.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Integer idEvent;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String venue;
    private Integer capacity;
    private BigDecimal pricePerTicket;
    private Integer availableSpots;
}