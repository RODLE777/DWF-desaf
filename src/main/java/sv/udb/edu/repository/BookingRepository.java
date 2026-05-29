package sv.udb.edu.repository;

import sv.udb.edu.entity.Booking;
import sv.udb.edu.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    /**
     * Lista todas las reservas del usuario autenticado.
     * Usado en GET /api/bookings/my
     */
    List<Booking> findByUser_IdUser(Integer userId);

    /**
     * Calcula la suma de entradas confirmadas para un evento.
     * Usado para validar disponibilidad en BookingService.
     */
    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Booking b " +
            "WHERE b.event.idEvent = :eventId AND b.status = :status")
    Integer sumQuantityByEventIdAndStatus(Integer eventId, BookingStatus status);
}