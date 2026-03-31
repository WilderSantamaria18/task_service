package com.idat.pe.task_service.dto;

import com.idat.pe.task_service.entity.Tarea.Estado;
import com.idat.pe.task_service.entity.Tarea.Prioridad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de tarea
 * Sprint 2: T-201
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TareaResponse {

    private Integer id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaLimite;
    private Prioridad prioridad;
    private Estado estado;
    private Integer usuarioId;
    private String googleEventId;
    private String googleEventLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
