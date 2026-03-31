package com.idat.pe.task_service.repository;

import com.idat.pe.task_service.entity.Tarea;
import com.idat.pe.task_service.entity.Tarea.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Integer> {
    
    // Buscar tareas por usuario
    List<Tarea> findByUsuarioId(Integer usuarioId);
    
    // Buscar tareas por usuario y estado (para filtrados)
    List<Tarea> findByUsuarioIdAndEstado(Integer usuarioId, Estado estado);
}
