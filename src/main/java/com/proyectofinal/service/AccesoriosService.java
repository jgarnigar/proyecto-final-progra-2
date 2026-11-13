package com.proyectofinal.service;

import com.proyectofinal.DAO.AccesoriosDAO;
import com.proyectofinal.modelo.Accesorios;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccesoriosService {

    private final AccesoriosDAO accesoriosDAO;

    public AccesoriosService(AccesoriosDAO accesoriosDAO) {
        this.accesoriosDAO = accesoriosDAO;
    }

    public Accesorios registrarAccesorio(Accesorios accesorio) {
        validarReglasDeNegocio(accesorio);

        if (!accesoriosDAO.registrar(accesorio)) {
            throw new RuntimeException("Error interno: No se pudo registrar el accesorio.");
        }
        return accesorio;
    }

    public Accesorios modificarAccesorio(int id, Accesorios accesorioActualizado) {
        Accesorios accesorioExistente = accesoriosDAO.consultar(id);
        if (accesorioExistente == null) {
            throw new IllegalArgumentException("No se encontró el accesorio con ID: " + id);
        }

        validarReglasDeNegocio(accesorioActualizado);
        accesorioActualizado.setIdAccesorio(id);

        if (!accesoriosDAO.modificar(accesorioActualizado)) {
            throw new RuntimeException("Error interno: No se pudo actualizar el accesorio.");
        }
        return accesorioActualizado;
    }

    private void validarReglasDeNegocio(Accesorios accesorio) {
        if (accesorio.getNombre() == null || accesorio.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del accesorio es obligatorio.");
        }
        if (accesorio.getTarifaFija() == null || accesorio.getTarifaFija().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tarifa fija no puede ser negativa.");
        }
        if (accesorio.getPrecioReposicion() == null || accesorio.getPrecioReposicion().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de reposición debe ser mayor que cero.");
        }
        if (accesorio.getExistenciaActual() < 0) {
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }
    }
}