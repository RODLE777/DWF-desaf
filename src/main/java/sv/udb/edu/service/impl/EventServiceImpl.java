package sv.udb.edu.service.impl;

import sv.udb.edu.dto.request.EventRequest;
import sv.udb.edu.dto.response.EventResponse;
import sv.udb.edu.entity.Event;
import sv.udb.edu.enums.BookingStatus;
import sv.udb.edu.exception.ResourceNotFoundException;
import sv.udb.edu.repository.BookingRepository;
import sv.udb.edu.repository.EventRepository;
import sv.udb.edu.service.interfaces.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacion del servicio de eventos.
 * Punto Extra: usa Pageable para paginacion en getAllEvents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Integer id) {
        Event event = findEventOrThrow(id);
        return mapToResponse(event);
    }

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .venue(request.getVenue())
                .capacity(request.getCapacity())
                .pricePerTicket(request.getPricePerTicket())
                .build();

        Event saved = eventRepository.save(event);
        log.info("Evento creado con ID: {}", saved.getIdEvent());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Integer id, EventRequest request) {
        Event event = findEventOrThrow(id);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setVenue(request.getVenue());
        event.setCapacity(request.getCapacity());
        event.setPricePerTicket(request.getPricePerTicket());

        Event updated = eventRepository.save(event);
        log.info("Evento actualizado con ID: {}", updated.getIdEvent());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteEvent(Integer id) {
        Event event = findEventOrThrow(id);
        eventRepository.delete(event);
        log.info("Evento eliminado con ID: {}", id);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private Event findEventOrThrow(Integer id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));
    }

    private EventResponse mapToResponse(Event event) {
        // Calcula los cupos disponibles (capacity - entradas confirmadas)
        Integer bookedTickets = bookingRepository.sumQuantityByEventIdAndStatus(
                event.getIdEvent(), BookingStatus.CONFIRMED);
        int available = event.getCapacity() - (bookedTickets == null ? 0 : bookedTickets);

        return EventResponse.builder()
                .idEvent(event.getIdEvent())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .venue(event.getVenue())
                .capacity(event.getCapacity())
                .pricePerTicket(event.getPricePerTicket())
                .availableSpots(Math.max(0, available))
                .build();
    }
}