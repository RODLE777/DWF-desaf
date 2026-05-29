package sv.udb.edu.service.interfaces;

import sv.udb.edu.dto.request.EventRequest;
import sv.udb.edu.dto.response.EventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {
    Page<EventResponse> getAllEvents(Pageable pageable);
    EventResponse getEventById(Integer id);
    EventResponse createEvent(EventRequest request);
    EventResponse updateEvent(Integer id, EventRequest request);
    void deleteEvent(Integer id);
}