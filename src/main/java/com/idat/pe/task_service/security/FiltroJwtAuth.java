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
            
            if (token != null && jwtService.validarToken(token)) {
                Claims claims = jwtService.obtenerClaims(token);
                jwtService.generarAutenticacion(claims);
            } else {
                SecurityContextHolder.clearContext();
            }
            
            filterChain.doFilter(request, response);
            
        } catch (JwtException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        }
    }
}
