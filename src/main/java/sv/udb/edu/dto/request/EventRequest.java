package sv.udb.edu.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull(message = "La fecha del evento es obligatoria")
    @Future(message = "La fecha del evento debe ser futura")
    private LocalDateTime eventDate;

    @NotBlank(message = "El lugar es obligatorio")
    @Size(max = 255)
    private String venue;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor a 0")
    private Integer capacity;

    @NotNull(message = "El precio por entrada es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal pricePerTicket;
}
