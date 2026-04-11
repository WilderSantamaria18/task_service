package com.idat.pe.task_service.remote.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDto {
    private Integer id;
    private String nombre;
    private String email;
    private String role;
}
