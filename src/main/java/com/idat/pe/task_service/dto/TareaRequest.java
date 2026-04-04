package com.idat.pe.task_service.dto;

import com.idat.pe.task_service.entity.Tarea.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * DTO para solicitud de creación/actualización de tarea
 * Sprint 2: T-201
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TareaRequest {

    @NotBlank(message = "El título es requerido")
    private String titulo;

    @NotBlank(message = "La descripción es requerida")
    private String descripcion;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private java.time.LocalDateTime fechaLimite;

    @NotNull(message = "La prioridad es requerida")
    private Prioridad prioridad;

    // Campos opcionales para integración con Google Calendar (sin OAuth)
    private String googleEventId;
    private String googleEventLink;
}
