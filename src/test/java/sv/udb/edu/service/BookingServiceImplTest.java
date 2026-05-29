package sv.udb.edu.service;
import sv.udb.edu.dto.request.BookingRequest;
import sv.udb.edu.dto.response.BookingResponse;
import sv.udb.edu.entity.Booking;
import sv.udb.edu.entity.Event;
import sv.udb.edu.entity.User;
import sv.udb.edu.enums.BookingStatus;
import sv.udb.edu.enums.Role;
import sv.udb.edu.exception.BusinessException;
import sv.udb.edu.exception.ResourceNotFoundException;
import sv.udb.edu.repository.BookingRepository;
import sv.udb.edu.repository.EventRepository;
import sv.udb.edu.repository.UserRepository;
import sv.udb.edu.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para BookingServiceImpl.
 * Cubre la logica de negocio clave:
 * - Calculo automatico de total_amount
 * - Validacion de disponibilidad de cupos
 * - Cancelacion (no elimina, cambia status)
 * - Filtro de reservas por usuario autenticado
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService - Tests Unitarios")
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private EventRepository   eventRepository;
    @Mock private UserRepository    userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User    testUser;
    private Event   testEvent;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .idUser(1)
                .username("johndoe")
                .firstname("John")
                .lastname("Doe")
                .password("hashed")
                .role(Role.ROLE_USER)
                .build();

        testEvent = Event.builder()
                .idEvent(10)
                .title("Concierto Rock")
                .venue("Estadio Cuscatlan")
                .eventDate(LocalDateTime.now().plusDays(30))
                .capacity(100)
                .pricePerTicket(new BigDecimal("15.00"))
                .build();

        testBooking = Booking.builder()
                .idBooking(100)
                .event(testEvent)
                .user(testUser)
                .quantity(3)
                .totalAmount(new BigDecimal("45.00"))
                .bookingDate(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();
    }

    // ============================================================
    // CREATE BOOKING
    // ============================================================
    @Nested
    @DisplayName("createBooking()")
    class CreateBookingTests {

        @Test
        @DisplayName("Crea reserva exitosamente y calcula total_amount automaticamente")
        void createBooking_Success_CalculatesTotalAmountAutomatically() {
            // BookingRequest(eventId, quantity) — orden del @AllArgsConstructor
            BookingRequest request = new BookingRequest(10, 3);

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(eventRepository.findById(10)).thenReturn(Optional.of(testEvent));
            // 20 entradas ya confirmadas de 100 disponibles → hay cupo
            when(bookingRepository.sumQuantityByEventIdAndStatus(10, BookingStatus.CONFIRMED))
                    .thenReturn(20);
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

            BookingResponse response = bookingService.createBooking(request, "johndoe");

            assertThat(response).isNotNull();
            // total_amount = 3 entradas * $15.00 = $45.00
            assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("45.00"));
            assertThat(response.getQuantity()).isEqualTo(3);
            assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

            // El total_amount debe calcularse ANTES de persistir, nunca lo envia el cliente
            verify(bookingRepository).save(argThat(b ->
                    b.getTotalAmount().compareTo(new BigDecimal("45.00")) == 0
            ));
        }

        @Test
        @DisplayName("Falla cuando no hay cupos suficientes - lanza BusinessException con detalle")
        void createBooking_NoAvailableSpots_ThrowsBusinessException() {
            BookingRequest request = new BookingRequest(10, 10);

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(eventRepository.findById(10)).thenReturn(Optional.of(testEvent));
            // 95 confirmadas + 10 solicitadas = 105 > 100
            when(bookingRepository.sumQuantityByEventIdAndStatus(10, BookingStatus.CONFIRMED))
                    .thenReturn(95);

            assertThatThrownBy(() -> bookingService.createBooking(request, "johndoe"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cupos")
                    .hasMessageContaining("Solicitados: 10")
                    .hasMessageContaining("Disponibles: 5");

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Falla cuando el evento no existe - lanza ResourceNotFoundException")
        void createBooking_EventNotFound_ThrowsResourceNotFoundException() {
            BookingRequest request = new BookingRequest(999, 2);

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(eventRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.createBooking(request, "johndoe"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Falla cuando el usuario no existe - lanza ResourceNotFoundException")
        void createBooking_UserNotFound_ThrowsResourceNotFoundException() {
            BookingRequest request = new BookingRequest(10, 2);

            when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.createBooking(request, "nobody"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Calcula total como cero para evento gratuito (pricePerTicket = 0)")
        void createBooking_FreeEvent_TotalAmountIsZero() {
            testEvent.setPricePerTicket(BigDecimal.ZERO);
            BookingRequest request = new BookingRequest(10, 5);

            Booking freeBooking = Booking.builder()
                    .idBooking(101).event(testEvent).user(testUser)
                    .quantity(5).totalAmount(BigDecimal.ZERO)
                    .status(BookingStatus.CONFIRMED).bookingDate(LocalDateTime.now())
                    .build();

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(eventRepository.findById(10)).thenReturn(Optional.of(testEvent));
            when(bookingRepository.sumQuantityByEventIdAndStatus(10, BookingStatus.CONFIRMED))
                    .thenReturn(0);
            when(bookingRepository.save(any())).thenReturn(freeBooking);

            BookingResponse response = bookingService.createBooking(request, "johndoe");

            assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Acepta reserva cuando los cupos son exactamente suficientes (limite)")
        void createBooking_ExactCapacity_Success() {
            BookingRequest request = new BookingRequest(10, 5);

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(eventRepository.findById(10)).thenReturn(Optional.of(testEvent));
            // 95 + 5 = 100 = capacity exacto, debe pasar sin lanzar excepcion
            when(bookingRepository.sumQuantityByEventIdAndStatus(10, BookingStatus.CONFIRMED))
                    .thenReturn(95);
            when(bookingRepository.save(any())).thenReturn(testBooking);

            assertThatCode(() -> bookingService.createBooking(request, "johndoe"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Falla cuando la reserva supera por 1 la capacidad (limite + 1)")
        void createBooking_ExceedsCapacityByOne_ThrowsBusinessException() {
            BookingRequest request = new BookingRequest(10, 6);

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(eventRepository.findById(10)).thenReturn(Optional.of(testEvent));
            // 95 + 6 = 101 > 100
            when(bookingRepository.sumQuantityByEventIdAndStatus(10, BookingStatus.CONFIRMED))
                    .thenReturn(95);

            assertThatThrownBy(() -> bookingService.createBooking(request, "johndoe"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Cuando sumQuantityByEventIdAndStatus retorna null (sin reservas) lo trata como 0")
        void createBooking_NullFromRepository_TreatedAsZeroBooked() {
            BookingRequest request = new BookingRequest(10, 3);

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(eventRepository.findById(10)).thenReturn(Optional.of(testEvent));
            // Repositorio puede retornar null si no hay reservas previas
            when(bookingRepository.sumQuantityByEventIdAndStatus(10, BookingStatus.CONFIRMED))
                    .thenReturn(null);
            when(bookingRepository.save(any())).thenReturn(testBooking);

            assertThatCode(() -> bookingService.createBooking(request, "johndoe"))
                    .doesNotThrowAnyException();
        }
    }

    // ============================================================
    // GET MY BOOKINGS
    // ============================================================
    @Nested
    @DisplayName("getMyBookings()")
    class GetMyBookingsTests {

        @Test
        @DisplayName("Retorna solo las reservas del usuario autenticado")
        void getMyBookings_ReturnsOnlyAuthenticatedUserBookings() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(bookingRepository.findByUser_IdUser(1)).thenReturn(List.of(testBooking));

            List<BookingResponse> result = bookingService.getMyBookings("johndoe");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUsername()).isEqualTo("johndoe");
            assertThat(result.get(0).getIdBooking()).isEqualTo(100);

            // Verifica que el filtro se aplica por el ID del usuario correcto
            verify(bookingRepository).findByUser_IdUser(1);
        }

        @Test
        @DisplayName("Retorna lista vacia si el usuario no tiene reservas")
        void getMyBookings_NoBookings_ReturnsEmptyList() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(bookingRepository.findByUser_IdUser(1)).thenReturn(List.of());

            List<BookingResponse> result = bookingService.getMyBookings("johndoe");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Falla si el usuario no existe en BD")
        void getMyBookings_UserNotFound_ThrowsResourceNotFoundException() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getMyBookings("unknown"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("El response mapea correctamente todos los campos de la reserva")
        void getMyBookings_ResponseMapsAllFields() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
            when(bookingRepository.findByUser_IdUser(1)).thenReturn(List.of(testBooking));

            List<BookingResponse> result = bookingService.getMyBookings("johndoe");

            BookingResponse r = result.get(0);
            assertThat(r.getIdBooking()).isEqualTo(100);
            assertThat(r.getEventId()).isEqualTo(10);
            assertThat(r.getEventTitle()).isEqualTo("Concierto Rock");
            assertThat(r.getEventVenue()).isEqualTo("Estadio Cuscatlan");
            assertThat(r.getTotalAmount()).isEqualByComparingTo(new BigDecimal("45.00"));
            assertThat(r.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }
    }

    // ============================================================
    // CANCEL BOOKING
    // ============================================================
    @Nested
    @DisplayName("cancelBooking()")
    class CancelBookingTests {

        @Test
        @DisplayName("Cancelacion exitosa: cambia status a CANCELLED sin eliminar el registro")
        void cancelBooking_Success_ChangesStatusToCancelled() {
            when(bookingRepository.findById(100)).thenReturn(Optional.of(testBooking));
            // El save retorna la misma entidad ya modificada
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

            BookingResponse response = bookingService.cancelBooking(100, "johndoe");

            assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            // Nunca debe llamarse a delete ni deleteById
            verify(bookingRepository, never()).delete(any());
            verify(bookingRepository, never()).deleteById(any());
            verify(bookingRepository).save(argThat(b ->
                    b.getStatus() == BookingStatus.CANCELLED
            ));
        }

        @Test
        @DisplayName("Falla si un usuario intenta cancelar la reserva de otro usuario")
        void cancelBooking_NotOwner_ThrowsBusinessException() {
            when(bookingRepository.findById(100)).thenReturn(Optional.of(testBooking));

            assertThatThrownBy(() -> bookingService.cancelBooking(100, "otheruser"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("permisos");

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Falla si se intenta cancelar una reserva ya cancelada")
        void cancelBooking_AlreadyCancelled_ThrowsBusinessException() {
            testBooking.setStatus(BookingStatus.CANCELLED);
            when(bookingRepository.findById(100)).thenReturn(Optional.of(testBooking));

            assertThatThrownBy(() -> bookingService.cancelBooking(100, "johndoe"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya fue cancelada");

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Falla si la reserva no existe - lanza ResourceNotFoundException")
        void cancelBooking_BookingNotFound_ThrowsResourceNotFoundException() {
            when(bookingRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.cancelBooking(999, "johndoe"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }
}
