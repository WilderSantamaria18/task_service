package com.idat.pe.task_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class FiltroJwtAuth extends OncePerRequestFilter {

    private final IJwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtService.extraerTokenUsuario(request);
            
            if (token == null) {
                // Si no hay token, continuar sin autenticación
                // El SecurityConfig rechazará con 403 si /api/tareas requiere autenticación
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            
            if (jwtService.validarToken(token)) {
                Claims claims = jwtService.obtenerClaims(token);
                jwtService.generarAutenticacion(claims);
            } else {
                // Token inválido o expirado
                SecurityContextHolder.clearContext();
            }
            
            filterChain.doFilter(request, response);
            
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token inválido: " + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Error en validación JWT: " + ex.getMessage() + "\"}");
        }
    }
}
