package com.idat.pe.task_service.service;

import com.idat.pe.task_service.dto.TareaRequest;
import com.idat.pe.task_service.dto.TareaResponse;
import com.idat.pe.task_service.entity.Tarea;
import com.idat.pe.task_service.entity.Tarea.Estado;
import com.idat.pe.task_service.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Tarea con CRUD y validación de autorización
 * Sprint 2: T-202 - Validación que cada usuario acceda solo sus tareas
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TareaService {

    private final TareaRepository tareaRepository;
    private final GoogleCalendarService googleCalendarService;



    /**
     * GET /api/tareas - Listar todas las tareas del usuario actual
     * Con filtro opcional por estado
     */
    public List<TareaResponse> listarTareasPorUsuario(Integer usuarioId, Estado estado) {
        List<Tarea> tareas;

        if (estado != null) {
            tareas = tareaRepository.findByUsuarioIdAndEstado(usuarioId, estado);
        } else {
            tareas = tareaRepository.findByUsuarioId(usuarioId);
        }

        return tareas.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET /api/tareas/{id} - Obtener una tarea específica
     * Valida que pertenezca al usuario actual
     */
    public TareaResponse obtenerTarea(Integer id, Integer usuarioId) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        // Validar autorización
        if (!tarea.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado: La tarea no pertenece al usuario actual");
        }

        return convertirAResponse(tarea);
    }

    /**
     * POST /api/tareas - Crear una nueva tarea
     * Automáticamente asigna el usuarioId del token
     */
    public TareaResponse crearTarea(TareaRequest request, Integer usuarioId) {
        Tarea tarea = Tarea.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .fechaLimite(request.getFechaLimite())
                .prioridad(request.getPrioridad())
                .estado(Estado.PENDIENTE) // Por defecto PENDIENTE
                .usuarioId(usuarioId)
                .googleEventId(request.getGoogleEventId())
                .googleEventLink(request.getGoogleEventLink())
                .build();

        Tarea tareaGuardada = tareaRepository.save(tarea);

        // Sincronizar con Google Calendar
        try {
            com.google.api.services.calendar.model.Event event = googleCalendarService.crearEvento(tareaGuardada);
            if (event != null) {
                tareaGuardada.setGoogleEventId(event.getId());
                tareaGuardada.setGoogleEventLink(event.getHtmlLink());
                tareaRepository.save(tareaGuardada);
            }
        } catch (Exception e) {
            System.err.println("No se pudo sincronizar con Google Calendar");
        }

        return convertirAResponse(tareaGuardada);
    }

    /**
     * PUT /api/tareas/{id} - Actualizar una tarea
     * Solo es posible si el usuario es el propietario
     */
    public TareaResponse actualizarTarea(Integer id, TareaRequest request, Integer usuarioId) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        // Validar autorización
        if (!tarea.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado: La tarea no pertenece al usuario actual");
        }

        // Actualizar campos
        tarea.setTitulo(request.getTitulo());
        tarea.setDescripcion(request.getDescripcion());
        tarea.setFechaLimite(request.getFechaLimite());
        tarea.setPrioridad(request.getPrioridad());
        tarea.setGoogleEventId(request.getGoogleEventId());
        tarea.setGoogleEventLink(request.getGoogleEventLink());

        Tarea tareaActualizada = tareaRepository.save(tarea);

        // Sincronizar con Google Calendar
        try {
            com.google.api.services.calendar.model.Event event = googleCalendarService
                    .actualizarEvento(tareaActualizada);
            if (event != null && (tareaActualizada.getGoogleEventId() == null)) {
                tareaActualizada.setGoogleEventId(event.getId());
                tareaActualizada.setGoogleEventLink(event.getHtmlLink());
                tareaRepository.save(tareaActualizada);
            }
        } catch (Exception e) {
            System.err.println("No se pudo sincronizar actualización con Google Calendar");
        }

        return convertirAResponse(tareaActualizada);
    }

    /**
     * DELETE /api/tareas/{id} - Eliminar una tarea
     */
    public void eliminarTarea(Integer id, Integer usuarioId) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        // Validar autorización
        if (!tarea.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado: La tarea no pertenece al usuario actual");
        }

        // Eliminar evento de Google Calendar
        if (tarea.getGoogleEventId() != null) {
            googleCalendarService.eliminarEvento(tarea.getGoogleEventId());
        }

        tareaRepository.delete(tarea);
    }

    /**
     * PATCH /api/tareas/{id}/estado - Cambiar estado de la tarea
     * Permite cambiar entre PENDIENTE y COMPLETADA
     */
    public TareaResponse cambiarEstado(Integer id, Estado nuevoEstado, Integer usuarioId) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        // Validar autorización
        if (!tarea.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado: La tarea no pertenece al usuario actual");
        }

        tarea.setEstado(nuevoEstado);
        Tarea tareaActualizada = tareaRepository.save(tarea);
        return convertirAResponse(tareaActualizada);
    }

    /**
     * Convertir entidad Tarea a DTO TareaResponse
     */
    private TareaResponse convertirAResponse(Tarea tarea) {
        return TareaResponse.builder()
                .id(tarea.getId())
                .titulo(tarea.getTitulo())
                .descripcion(tarea.getDescripcion())
                .fechaLimite(tarea.getFechaLimite())
                .prioridad(tarea.getPrioridad())
                .estado(tarea.getEstado())
                .usuarioId(tarea.getUsuarioId())
                .googleEventId(tarea.getGoogleEventId())
                .googleEventLink(tarea.getGoogleEventLink())
                .createdAt(tarea.getCreatedAt())
                .updatedAt(tarea.getUpdatedAt())
                .build();
    }
}
