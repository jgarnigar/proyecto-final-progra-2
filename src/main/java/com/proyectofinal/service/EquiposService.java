package com.proyectofinal.service;

import com.proyectofinal.DAO.EquiposDAO;
import com.proyectofinal.modelo.Equipos;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class EquiposService {

    private final EquiposDAO equiposDAO;

    public EquiposService(EquiposDAO equiposDAO) {
        this.equiposDAO = equiposDAO;
    }

    public Equipos registrarEquipo(Equipos equipo) {
        // Validar campos de texto obligatorios
        if (equipo.getTipo() == null || equipo.getTipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de equipo (ej. Bicicleta eléctrica, Scooter) es obligatorio.");
        }
        if (equipo.getModelo() == null || equipo.getModelo().isBlank()) {
            throw new IllegalArgumentException("El modelo del equipo es obligatorio.");
        }

        //  Validar temas monetarios
        if (equipo.getTarifaHora() == null || equipo.getTarifaHora().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La tarifa por hora debe ser mayor a cero.");
        }
        if (equipo.getPrecioReposicion() == null || equipo.getPrecioReposicion().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de reposición debe ser mayor a cero.");
        }

        //  La batería no puede ser mayor a 100 ni menor a 0
        if (equipo.getBateriaPorcentaje() < 0 || equipo.getBateriaPorcentaje() > 100) {
            throw new IllegalArgumentException("El porcentaje de batería debe estar entre 0 y 100.");
        }

        //  Si no se envía un estado, por defecto está disponible para alquilar
        if (equipo.getEstado() == null || equipo.getEstado().isBlank()) {
            equipo.setEstado("DISPONIBLE");
        }

        // Que el DAO trabaje
        boolean registrado = equiposDAO.registrar(equipo);
        if (!registrado) {
            throw new RuntimeException("Error interno: No se pudo guardar el equipo en la base de datos.");
        }

        return equipo;
    }
}