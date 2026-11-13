package com.proyectofinal.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.proyectofinal.DAO.EquiposDAO;
import com.proyectofinal.DAO.MantenimientoDAO;
import com.proyectofinal.modelo.Equipos;
import com.proyectofinal.modelo.Mantenimientos;

@Service
public class InventarioService {
    private final EquiposDAO equiposDAO;
    private final MantenimientoDAO mantenimientoDAO;

    public InventarioService(EquiposDAO equiposDAO, MantenimientoDAO mantenimientoDAO) {
        this.equiposDAO = equiposDAO;
        this.mantenimientoDAO = mantenimientoDAO;
    }

    public Mantenimientos registrarMantenimiento(Mantenimientos mantenimiento) {

        Equipos equipo = equiposDAO.consultar(mantenimiento.getIdEquipo());

        if (equipo == null) {
            throw new RuntimeException("El equipo no existe.");
        }

        if (!"DISPONIBLE".equals(equipo.getEstado())) {
            throw new RuntimeException("Solo se pueden enviar a mantenimiento equipos disponibles.");
        }

        if (mantenimiento.getMotivo() == null || mantenimiento.getMotivo().isBlank()) {
            throw new RuntimeException("Debe ingresar el motivo del mantenimiento.");
        }

        if (mantenimiento.getMotivo().length() > 100) {
            throw new RuntimeException("El motivo no puede superar 100 caracteres.");
        }

        if (mantenimiento.getCosto() == null || mantenimiento.getCosto().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El costo no puede ser negativo.");
        }

        if (mantenimiento.getFecha() == null) {
            mantenimiento.setFecha(LocalDateTime.now());
        }

        boolean registrado = mantenimientoDAO.registrarMantenimiento(mantenimiento);

        if (!registrado) {
            throw new RuntimeException("No se pudo registrar el mantenimiento.");
        }

        boolean actualizado = equiposDAO.actualizarEstado(equipo.getIdEquipo(), "EN MANTENIMIENTO");

        if (!actualizado) {
            throw new RuntimeException("No se pudo actualizar el estado del equipo.");
        }

        return mantenimiento;
    }

    public List<Mantenimientos> obtenerHistorial(int idEquipo) {
        if (equiposDAO.consultar(idEquipo) == null) {
            throw new RuntimeException("El equipo no existe.");
        }

        return mantenimientoDAO.obtenerMantenimientosPorEquipo(idEquipo);
    }

    public void cerrarMantenimiento(int idEquipo) {
        Equipos equipo = equiposDAO.consultar(idEquipo);

        if (equipo == null) {
            throw new RuntimeException("El equipo no existe.");
        }

        if (!"EN MANTENIMIENTO".equals(equipo.getEstado())) {
            throw new RuntimeException( "El equipo no se encuentra en mantenimiento.");
        }

        boolean actualizado = equiposDAO.actualizarEstado(idEquipo, "DISPONIBLE");

        if (!actualizado) {
            throw new RuntimeException("No se pudo cerrar el mantenimiento.");
        }
    }
}