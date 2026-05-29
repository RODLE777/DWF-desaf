package sv.udb.edu.service.impl;

import sv.udb.edu.dto.request.BookingRequest;
import sv.udb.edu.dto.response.BookingResponse;
import sv.udb.edu.entity.Booking;
import sv.udb.edu.entity.Event;
import sv.udb.edu.entity.User;
import sv.udb.edu.enums.BookingStatus;
import sv.udb.edu.exception.BusinessException;
import sv.udb.edu.exception.ResourceNotFoundException;
import sv.udb.edu.repository.BookingRepository;
import sv.udb.edu.repository.EventRepository;
import sv.udb.edu.repository.UserRepository;
import sv.udb.edu.service.interfaces.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementacion de la logica de negocio de Reservas.
 *
 * Logica de negocio clave:
 *
 * 1. CALCULO DE total_amount:
 *    - Se calcula automaticamente en el service: quantity * event.pricePerTicket
 *    - El cliente NUNCA envia este campo; se ignora cualquier valor que envie.
 *
 * 2. VALIDACION DE DISPONIBILIDAD:
 *    - Suma todas las entradas CONFIRMADAS para el evento.
 *    - Si (reservas_actuales + nueva_cantidad) > capacity => error 400.
 *    - Se usa BookingRepository.sumQuantityByEventIdAndStatus para eficiencia.
 *
 * 3. MIS RESERVAS (GET /api/bookings/my):
 *    - Se extrae el usuario del SecurityContext a traves del username del JWT.
 *    - El controller pasa el username al service, que lo usa para filtrar reservas.
 *
 * 4. CANCELACION:
 *    - No elimina el registro de BD.
 *    - Solo cambia status de CONFIRMED a CANCELLED.
 *    - Solo el dueno de la reserva puede cancelarla.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        // 1. Cargar el usuario autenticado (extraido del JWT en el controller)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Cargar el evento solicitado
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento", request.getEventId()));

        // 3. Validar disponibilidad de cupos
        Integer totalBookedTickets = bookingRepository.sumQuantityByEventIdAndStatus(
                event.getIdEvent(), BookingStatus.CONFIRMED);
        int currentBooked = totalBookedTickets != null ? totalBookedTickets : 0;
        int requestedQuantity = request.getQuantity();

        if (currentBooked + requestedQuantity > event.getCapacity()) {
            int available = event.getCapacity() - currentBooked;
            throw new BusinessException(
                    String.format("No hay suficientes cupos disponibles. Solicitados: %d, Disponibles: %d",
                            requestedQuantity, available)
            );
        }

        // 4. Calcular total_amount automaticamente (el cliente no lo envia)
        BigDecimal totalAmount = event.getPricePerTicket()
                .multiply(BigDecimal.valueOf(requestedQuantity));

        // 5. Crear y persistir la reserva
        Booking booking = Booking.builder()
                .event(event)
                .user(user)
                .quantity(requestedQuantity)
                .totalAmount(totalAmount)
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Reserva creada - ID: {}, Usuario: {}, Evento: {}, Total: {}",
                saved.getIdBooking(), username, event.getTitle(), totalAmount);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String username) {
        /*
         * El username viene del SecurityContext.getAuthentication().getName()
         * en el controller, que a su vez fue seteado por JwtAuthenticationFilter
         * al autenticar el token JWT del request.
         * Esto garantiza que cada usuario solo ve SUS propias reservas.
         */
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return bookingRepository.findByUser_IdUser(user.getIdUser())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Integer bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", bookingId));

        // Solo el dueno puede cancelar su propia reserva
        if (!booking.getUser().getUsername().equals(username)) {
            throw new BusinessException("No tienes permisos para cancelar esta reserva");
        }

        // No eliminar: solo cambiar status a CANCELLED
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Esta reserva ya fue cancelada anteriormente");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);

        log.info("Reserva cancelada - ID: {}, Usuario: {}", bookingId, username);
        return mapToResponse(updated);
    }

    // ============================================================
    // Helper de mapeo
    // ============================================================

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .idBooking(booking.getIdBooking())
                .eventId(booking.getEvent().getIdEvent())
                .eventTitle(booking.getEvent().getTitle())
                .eventVenue(booking.getEvent().getVenue())
                .eventDate(booking.getEvent().getEventDate())
                .username(booking.getUser().getUsername())
                .quantity(booking.getQuantity())
                .totalAmount(booking.getTotalAmount())
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus())
                .build();
    }
}
