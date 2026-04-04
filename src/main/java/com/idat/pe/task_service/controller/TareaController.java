package com.idat.pe.task_service.controller;

import com.idat.pe.task_service.dto.TareaRequest;
import com.idat.pe.task_service.dto.TareaResponse;
import com.idat.pe.task_service.entity.Tarea.Estado;
import com.idat.pe.task_service.service.TareaService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.List;

/**
 * Controller para gestión de Tareas
 * Sprint 2: T-203 - Endpoints CRUD con validación de autorización por JWT
 * 
 * Todos los endpoints requieren autenticación JWT
 * Cada usuario solo puede acceder/modificar sus propias tareas
 */
@RestController
@RequestMapping("/api/tareas")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Método mejorado: extrae el usuarioId desde el JWT
     * El JWT contiene un claim 'id' con el usuarioId
     */
    private Integer obtenerUsuarioIdDelToken(HttpServletRequest request) {
        try {
            // Extrae el token del header Authorization
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                throw new RuntimeException("Token no encontrado en el header");
            }

            String token = header.substring(7); // Quita "Bearer "

            // Parsea el JWT para obtener los claims
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extrae el 'id' del claim (que es el usuarioId)
            String usuarioIdStr = claims.getId();
            return Integer.parseInt(usuarioIdStr);

        } catch (NumberFormatException ex) {
            throw new RuntimeException("El ID del usuario en el token no es un número válido");
        } catch (Exception ex) {
            throw new RuntimeException("Error extrayendo usuarioId del token: " + ex.getMessage());
        }
    }

    /**
     * GET /api/tareas
     * Listar todas las tareas del usuario autenticado
     * 
     * Parámetros opcionales:
     * - estado: PENDIENTE o COMPLETADA (filtra por estado)
     */
    @GetMapping({ "", "/" })
    public ResponseEntity<List<TareaResponse>> listarTareas(
            HttpServletRequest request,
            @RequestParam(name = "estado", required = false) Estado estado) {

        Integer usuarioId = obtenerUsuarioIdDelToken(request);
        List<TareaResponse> tareas = tareaService.listarTareasPorUsuario(usuarioId, estado);

        return ResponseEntity.ok(tareas);
    }

    /**
     * GET /api/tareas/{id}
     * Obtener una tarea específica (solo si pertenece al usuario)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TareaResponse> obtenerTarea(
            HttpServletRequest request,
            @PathVariable Integer id) {
        Integer usuarioId = obtenerUsuarioIdDelToken(request);
        TareaResponse tarea = tareaService.obtenerTarea(id, usuarioId);

        return ResponseEntity.ok(tarea);
    }

    /**
     * POST /api/tareas
     * Crear una nueva tarea
     * El usuarioId se extrae automáticamente del JWT
     */
    @PostMapping({ "", "/" })
    public ResponseEntity<TareaResponse> crearTarea(
            HttpServletRequest request,
            @Valid @RequestBody TareaRequest tareaRequest) {
        Integer usuarioId = obtenerUsuarioIdDelToken(request);
        TareaResponse tarea = tareaService.crearTarea(tareaRequest, usuarioId);

        return ResponseEntity.status(HttpStatus.CREATED).body(tarea);
    }

    /**
     * PUT /api/tareas/{id}
     * Actualizar una tarea (solo si pertenece al usuario)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TareaResponse> actualizarTarea(
            HttpServletRequest request,
            @PathVariable Integer id,
            @Valid @RequestBody TareaRequest tareaRequest) {

        Integer usuarioId = obtenerUsuarioIdDelToken(request);
        TareaResponse tarea = tareaService.actualizarTarea(id, tareaRequest, usuarioId);

        return ResponseEntity.ok(tarea);
    }

    /**
     * DELETE /api/tareas/{id}
     * Eliminar una tarea (solo si pertenece al usuario)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(
            HttpServletRequest request,
            @PathVariable Integer id) {
        Integer usuarioId = obtenerUsuarioIdDelToken(request);
        tareaService.eliminarTarea(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/tareas/{id}/estado
     * Cambiar el estado de una tarea (PENDIENTE -> COMPLETADA o viceversa)
     * 
     * Body: { "estado": "COMPLETADA" }
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TareaResponse> cambiarEstado(
            HttpServletRequest request,
            @PathVariable Integer id,
            @RequestBody EstadoRequest estadoRequest) {

        Integer usuarioId = obtenerUsuarioIdDelToken(request);
        TareaResponse tarea = tareaService.cambiarEstado(id, estadoRequest.getEstado(), usuarioId);

        return ResponseEntity.ok(tarea);
    }

    /**
     * DTO interno para cambiar estado
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EstadoRequest {
        private Estado estado;
    }
}
