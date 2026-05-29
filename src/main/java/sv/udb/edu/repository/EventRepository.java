package sv.udb.edu.repository;

import sv.udb.edu.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    /**
     * Punto Extra: Paginacion en GET /api/events usando Spring Data Pageable.
     */
    Page<Event> findAll(Pageable pageable);

    /**
     * Calcula el total de entradas reservadas (CONFIRMED) para un evento.
     * Usado para validar disponibilidad antes de confirmar una reserva.
     */
    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Booking b " +
            "WHERE b.event.idEvent = :eventId AND b.status = 'CONFIRMED'")
    Integer sumConfirmedBookingsByEventId(Integer eventId);
}

