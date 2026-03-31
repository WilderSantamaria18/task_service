package com.idat.pe.task_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Cliente Feign para comunicarse con auth_service
 * Permite validar usuarios y obtener información
 * 
 * Usa Eureka para descubrimiento de servicios (usa el nombre auth-service)
 */
@FeignClient(name = "auth-service")
public interface UsuarioClient {

    /**
     * Obtener información de un usuario
     * GET /api/usuarios/{usuarioId}
     * Requiere token JWT
     */
    @GetMapping("/api/usuarios/{usuarioId}")
    UsuarioDTO obtenerUsuario(
            @PathVariable("usuarioId") Integer usuarioId,
            @RequestHeader("Authorization") String token
    );

    /**
     * DTO simple para respuesta de usuario
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class UsuarioDTO {
        private Integer id;
        private String nombre;
        private String email;
        private String role;
    }
}
