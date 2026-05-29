package sv.udb.edu.controller;

import org.springdoc.core.annotations.ParameterObject;
import sv.udb.edu.dto.request.EventRequest;
import sv.udb.edu.dto.response.EventResponse;
import sv.udb.edu.service.interfaces.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Eventos.
 *
 * Punto Extra - Roles:
 * - GET (listar/ver): cualquier usuario autenticado (ROLE_USER o ROLE_ADMIN)
 * - POST, PUT, DELETE: solo ROLE_ADMIN
 *
 * Punto Extra - Paginacion:
 * - GET /api/events acepta parametros: ?page=0&size=10&sort=eventDate,asc
 *
 * Todos los endpoints requieren JWT valido en Authorization: Bearer <token>
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "CRUD de eventos. Crear/editar/eliminar requiere ROLE_ADMIN.")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(
            summary     = "Listar todos los eventos",
            description = "Retorna lista paginada de eventos. Params: page, size, sort. Requiere JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida"),
            @ApiResponse(responseCode = "403", description = "JWT no proporcionado o invalido")
    })
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @ParameterObject
            @Parameter(description = "Paginacion: page=0, size=10, sort=eventDate,asc")
            @PageableDefault(size = 10, sort = "eventDate", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(eventService.getAllEvents(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento por ID", description = "Requiere JWT valido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
            @ApiResponse(responseCode = "403", description = "JWT requerido")
    })
    public ResponseEntity<EventResponse> getEventById(@PathVariable Integer id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")   // Punto Extra: solo ADMIN
    @Operation(
            summary     = "Crear nuevo evento",
            description = "Solo ROLE_ADMIN puede crear eventos. Requiere JWT de administrador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "403", description = "Se requiere ROLE_ADMIN")
    })
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")   // Punto Extra: solo ADMIN
    @Operation(
            summary     = "Actualizar evento",
            description = "Solo ROLE_ADMIN puede modificar eventos. Requiere JWT de administrador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento actualizado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
            @ApiResponse(responseCode = "403", description = "Se requiere ROLE_ADMIN")
    })
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Integer id,
                                                     @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")   // Punto Extra: solo ADMIN
    @Operation(
            summary     = "Eliminar evento",
            description = "Solo ROLE_ADMIN puede eliminar eventos. Requiere JWT de administrador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento eliminado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
            @ApiResponse(responseCode = "403", description = "Se requiere ROLE_ADMIN")
    })
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}