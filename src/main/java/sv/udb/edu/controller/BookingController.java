package sv.udb.edu.controller;

import sv.udb.edu.dto.request.BookingRequest;
import sv.udb.edu.dto.response.BookingResponse;
import sv.udb.edu.service.interfaces.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Reservas.
 *
 * Uso de @AuthenticationPrincipal:
 * Spring Security inyecta automaticamente el UserDetails del usuario autenticado
 * que fue seteado en el SecurityContext por JwtAuthenticationFilter.
 * Desde ahi extraemos el username para pasarlo al service.
 * Esto garantiza que el usuario solo puede ver/cancelar SUS reservas.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Gestion de reservas. Todos los endpoints requieren JWT.")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(
            summary     = "Crear reserva",
            description = "Crea una reserva para el usuario autenticado. El total se calcula automaticamente. " +
                    "Requiere JWT valido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Sin cupos disponibles o datos invalidos"),
            @ApiResponse(responseCode = "403", description = "JWT requerido")
    })
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // El username se extrae del JWT del SecurityContext, no del body del request
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, userDetails.getUsername()));
    }

    @GetMapping("/my")
    @Operation(
            summary     = "Mis reservas",
            description = "Lista todas las reservas del usuario autenticado (extraido del JWT). " +
                    "Requiere JWT valido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reservas del usuario"),
            @ApiResponse(responseCode = "403", description = "JWT requerido")
    })
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getMyBookings(userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary     = "Cancelar reserva",
            description = "Cambia el status de la reserva a CANCELLED. No elimina el registro. " +
                    "Solo el dueno puede cancelar su propia reserva. Requiere JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada"),
            @ApiResponse(responseCode = "400", description = "Reserva ya cancelada"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para cancelar esta reserva"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, userDetails.getUsername()));
    }
}
