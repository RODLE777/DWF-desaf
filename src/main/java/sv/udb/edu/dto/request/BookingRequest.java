package sv.udb.edu.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "El ID del evento es obligatorio")
    private Integer eventId;

    @NotNull(message = "La cantidad de entradas es obligatoria")
    @Min(value = 1, message = "Debe reservar al menos una entrada")
    private Integer quantity;
}

