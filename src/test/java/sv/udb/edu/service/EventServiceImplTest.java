package sv.udb.edu.service;


import sv.udb.edu.dto.request.EventRequest;
import sv.udb.edu.dto.response.EventResponse;
import sv.udb.edu.entity.Event;
import sv.udb.edu.enums.BookingStatus;
import sv.udb.edu.exception.ResourceNotFoundException;
import sv.udb.edu.repository.BookingRepository;
import sv.udb.edu.repository.EventRepository;
import sv.udb.edu.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService - Tests Unitarios")
class EventServiceImplTest {

    @Mock private EventRepository   eventRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event       testEvent;
    private EventRequest eventRequest;

    @BeforeEach
    void setUp() {
        testEvent = Event.builder()
                .idEvent(1)
                .title("Concierto Rock")
                .description("Gran concierto")
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue("Estadio Cuscatlan")
                .capacity(500)
                .pricePerTicket(new BigDecimal("15.00"))
                .build();

        eventRequest = EventRequest.builder()
                .title("Concierto Rock")
                .description("Gran concierto")
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue("Estadio Cuscatlan")
                .capacity(500)
                .pricePerTicket(new BigDecimal("15.00"))
                .build();
    }

    // ============================================================
    // GET ALL EVENTS (paginacion)
    // ============================================================
    @Nested
    @DisplayName("getAllEvents()")
    class GetAllEventsTests {

        @Test
        @DisplayName("Retorna pagina de eventos con availableSpots calculados correctamente")
        void getAllEvents_ReturnsPaginatedEvents() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("eventDate").ascending());
            Page<Event> eventPage = new PageImpl<>(List.of(testEvent), pageable, 1);

            when(eventRepository.findAll(pageable)).thenReturn(eventPage);
            when(bookingRepository.sumQuantityByEventIdAndStatus(1, BookingStatus.CONFIRMED))
                    .thenReturn(50);

            Page<EventResponse> result = eventService.getAllEvents(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            // availableSpots = capacity(500) - booked(50) = 450
            assertThat(result.getContent().get(0).getAvailableSpots()).isEqualTo(450);
        }

        @Test
        @DisplayName("availableSpots es 0 cuando el evento esta completamente lleno")
        void getAllEvents_FullEvent_AvailableSpotsIsZero() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Event> eventPage = new PageImpl<>(List.of(testEvent), pageable, 1);

            when(eventRepository.findAll(pageable)).thenReturn(eventPage);
            when(bookingRepository.sumQuantityByEventIdAndStatus(1, BookingStatus.CONFIRMED))
                    .thenReturn(500);

            Page<EventResponse> result = eventService.getAllEvents(pageable);

            assertThat(result.getContent().get(0).getAvailableSpots()).isEqualTo(0);
        }

        @Test
        @DisplayName("availableSpots nunca es negativo aunque las reservas excedan la capacidad")
        void getAllEvents_OverBooked_AvailableSpotsIsZeroNotNegative() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Event> eventPage = new PageImpl<>(List.of(testEvent), pageable, 1);

            when(eventRepository.findAll(pageable)).thenReturn(eventPage);
            // Caso de corrupcion de datos: mas reservadas que la capacidad
            when(bookingRepository.sumQuantityByEventIdAndStatus(1, BookingStatus.CONFIRMED))
                    .thenReturn(600);

            Page<EventResponse> result = eventService.getAllEvents(pageable);

            // Math.max(0, available) garantiza que no sea negativo
            assertThat(result.getContent().get(0).getAvailableSpots()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Retorna pagina vacia si no hay eventos")
        void getAllEvents_NoEvents_ReturnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(eventRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

            Page<EventResponse> result = eventService.getAllEvents(pageable);

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    // ============================================================
    // GET BY ID
    // ============================================================
    @Nested
    @DisplayName("getEventById()")
    class GetEventByIdTests {

        @Test
        @DisplayName("Retorna evento existente correctamente mapeado")
        void getEventById_ExistingEvent_ReturnsResponse() {
            when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));
            when(bookingRepository.sumQuantityByEventIdAndStatus(1, BookingStatus.CONFIRMED))
                    .thenReturn(0);

            EventResponse response = eventService.getEventById(1);

            assertThat(response.getIdEvent()).isEqualTo(1);
            assertThat(response.getTitle()).isEqualTo("Concierto Rock");
            assertThat(response.getCapacity()).isEqualTo(500);
            // Sin reservas, todos los cupos estan disponibles
            assertThat(response.getAvailableSpots()).isEqualTo(500);
            assertThat(response.getPricePerTicket()).isEqualByComparingTo("15.00");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException para evento inexistente")
        void getEventById_NotFound_ThrowsException() {
            when(eventRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.getEventById(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // ============================================================
    // CREATE EVENT
    // ============================================================
    @Nested
    @DisplayName("createEvent()")
    class CreateEventTests {

        @Test
        @DisplayName("Crea evento correctamente y retorna response con el ID asignado")
        void createEvent_ValidRequest_ReturnsCreatedEvent() {
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
            when(bookingRepository.sumQuantityByEventIdAndStatus(anyInt(), any()))
                    .thenReturn(0);

            EventResponse response = eventService.createEvent(eventRequest);

            assertThat(response.getIdEvent()).isEqualTo(1);
            assertThat(response.getTitle()).isEqualTo("Concierto Rock");
            assertThat(response.getPricePerTicket()).isEqualByComparingTo("15.00");
            // Evento nuevo: todos los cupos disponibles
            assertThat(response.getAvailableSpots()).isEqualTo(500);

            verify(eventRepository).save(any(Event.class));
        }

        @Test
        @DisplayName("El evento creado mapea todos los campos del request correctamente")
        void createEvent_MapsAllFieldsFromRequest() {
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
                Event e = inv.getArgument(0);
                // Verificamos que el entity construido refleja el request
                assertThat(e.getTitle()).isEqualTo("Concierto Rock");
                assertThat(e.getVenue()).isEqualTo("Estadio Cuscatlan");
                assertThat(e.getCapacity()).isEqualTo(500);
                assertThat(e.getPricePerTicket()).isEqualByComparingTo("15.00");
                return testEvent;
            });
            when(bookingRepository.sumQuantityByEventIdAndStatus(anyInt(), any())).thenReturn(0);

            eventService.createEvent(eventRequest);

            verify(eventRepository).save(any(Event.class));
        }
    }

    // ============================================================
    // UPDATE EVENT
    // ============================================================
    @Nested
    @DisplayName("updateEvent()")
    class UpdateEventTests {

        @Test
        @DisplayName("Actualiza evento existente y retorna el response con los nuevos valores")
        void updateEvent_ExistingEvent_ReturnsUpdatedResponse() {
            EventRequest updateReq = EventRequest.builder()
                    .title("Concierto Rock Actualizado")
                    .description("Descripcion nueva")
                    .eventDate(LocalDateTime.now().plusDays(60))
                    .venue("Nuevo Venue")
                    .capacity(200)
                    .pricePerTicket(new BigDecimal("20.00"))
                    .build();

            Event updatedEvent = Event.builder()
                    .idEvent(1).title("Concierto Rock Actualizado")
                    .venue("Nuevo Venue").capacity(200)
                    .pricePerTicket(new BigDecimal("20.00"))
                    .eventDate(updateReq.getEventDate())
                    .description("Descripcion nueva")
                    .build();

            when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));
            when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);
            when(bookingRepository.sumQuantityByEventIdAndStatus(anyInt(), any())).thenReturn(0);

            EventResponse response = eventService.updateEvent(1, updateReq);

            assertThat(response.getTitle()).isEqualTo("Concierto Rock Actualizado");
            assertThat(response.getCapacity()).isEqualTo(200);
            assertThat(response.getPricePerTicket()).isEqualByComparingTo("20.00");
        }

        @Test
        @DisplayName("Falla al actualizar evento inexistente - lanza ResourceNotFoundException")
        void updateEvent_NotFound_ThrowsException() {
            when(eventRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.updateEvent(999, eventRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============================================================
    // DELETE EVENT
    // ============================================================
    @Nested
    @DisplayName("deleteEvent()")
    class DeleteEventTests {

        @Test
        @DisplayName("Elimina evento existente sin lanzar excepcion")
        void deleteEvent_ExistingEvent_DeletesSuccessfully() {
            when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));

            assertThatCode(() -> eventService.deleteEvent(1))
                    .doesNotThrowAnyException();

            verify(eventRepository).delete(testEvent);
        }

        @Test
        @DisplayName("Nunca llama a delete si el evento no existe")
        void deleteEvent_NotFound_NeverCallsDelete() {
            when(eventRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.deleteEvent(999))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(eventRepository, never()).delete(any());
        }
    }
}
