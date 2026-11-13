package com.proyectofinal.service;

import com.proyectofinal.DAO.AlquilerDAO;
import com.proyectofinal.DAO.ClientesDAO;
import com.proyectofinal.modelo.Alquileres;
import com.proyectofinal.modelo.Clientes;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AlquilerService {

    private final AlquilerDAO alquilerDAO;
    private final ClientesDAO clientesDAO;

    public AlquilerService(AlquilerDAO alquilerDAO, ClientesDAO clientesDAO) {
        this.alquilerDAO = alquilerDAO;
        this.clientesDAO = clientesDAO;
    }

    public Alquileres procesarNuevoAlquiler(Alquileres alquiler) {
        // Validar que el cliente exista y esté activo
        Clientes cliente = clientesDAO.consultar(alquiler.getIdCliente());
        if (cliente == null || "INACTIVO".equals(cliente.getEstado())) {
            throw new IllegalArgumentException("El cliente no existe o se encuentra inactivo.");
        }

        //  Bloquear alquileres si el cliente tiene un saldo deudor alto (ej. > 50.00)
        if (cliente.getSaldoPendiente() != null && cliente.getSaldoPendiente().compareTo(new BigDecimal("50.00")) > 0) {
            throw new IllegalStateException("El cliente tiene deudas previas excesivas y no puede realizar nuevos alquileres.");
        }

        //  Preparar el estado inicial del registro
        alquiler.setEstado("EN_CURSO");
        alquiler.setFechaReal(null); // Aún no se han devuelto los equipos

        //  Calcular los días de alquiler utilizando java.time
        long diasProyectados = ChronoUnit.DAYS.between(LocalDateTime.now(), alquiler.getFechaEstimada());
        if (diasProyectados < 1) {
            diasProyectados = 1; // Se cobra al menos un día mínimo
        }

        // Regla de Negocio: Si no se define un depósito, calcular uno base automáticamente
        if (alquiler.getDepositoCobrado() == null || alquiler.getDepositoCobrado().compareTo(BigDecimal.ZERO) <= 0) {
            // Ejemplo: Se asume un depósito base de 15.00 por cada día proyectado
            BigDecimal depositoPorDia = new BigDecimal("15.00");
            alquiler.setDepositoCobrado(depositoPorDia.multiply(BigDecimal.valueOf(diasProyectados)));
        }

        // Delegar la persistencia transaccional al DAO
        boolean registrado = alquilerDAO.registrarAlquiler(alquiler);
        if (!registrado) {
            throw new RuntimeException("Error interno: No se pudo guardar el alquiler en la base de datos.");
        }

        return alquiler;
    }
}