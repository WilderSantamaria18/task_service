package com.idat.pe.task_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tarea")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridad prioridad; // Enum: ALTA, MEDIA, BAJA

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado; // Enum: PENDIENTE, COMPLETADA

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "google_event_id")
    private String googleEventId;

    @Column(name = "google_event_link")
    private String googleEventLink;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Prioridad {
        ALTA, MEDIA, BAJA
    }

    public enum Estado {
        PENDIENTE, COMPLETADA
    }
}
