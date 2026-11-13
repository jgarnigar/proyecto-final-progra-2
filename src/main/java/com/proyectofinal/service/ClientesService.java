package com.proyectofinal.service;

import com.proyectofinal.DAO.ClientesDAO;
import com.proyectofinal.modelo.Clientes;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ClientesService {

    private final ClientesDAO clientesDAO;

    public ClientesService(ClientesDAO clientesDAO) {
        this.clientesDAO = clientesDAO;
    }

    public Clientes registrarCliente(Clientes cliente) {
        // Validaciones de negocio (campos obligatorios)
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email del cliente es obligatorio.");
        }

        //  Valores por defecto para un cliente nuevo
        if (cliente.getSaldoPendiente() == null) {
            cliente.setSaldoPendiente(BigDecimal.ZERO);
        }
        if (cliente.getEstado() == null || cliente.getEstado().isBlank()) {
            cliente.setEstado("ACTIVO");
        }

        boolean registrado = clientesDAO.registrar(cliente);
        if (!registrado) {
            throw new RuntimeException("Error interno: No se pudo registrar el cliente en la base de datos.");
        }

        return cliente;
    }

    public Clientes modificarCliente(int id, Clientes clienteActualizado) {

        Clientes clienteExistente = clientesDAO.consultar(id);
        if (clienteExistente == null) {
            throw new IllegalArgumentException("No se encontró el cliente con ID: " + id);
        }

        if (clienteActualizado.getNombre() == null || clienteActualizado.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        if (clienteActualizado.getEmail() == null || clienteActualizado.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email del cliente es obligatorio.");
        }

        clienteActualizado.setIdCliente(id);

        if (clienteActualizado.getSaldoPendiente() == null) {

            clienteActualizado.setSaldoPendiente(clienteExistente.getSaldoPendiente());
        }
        if (clienteActualizado.getEstado() == null || clienteActualizado.getEstado().isBlank()) {
            clienteActualizado.setEstado(clienteExistente.getEstado());
        }

        boolean modificado = clientesDAO.modificar(clienteActualizado);
        if (!modificado) {
            throw new RuntimeException("Error interno: No se pudo actualizar el cliente.");
        }

        return clienteActualizado;
    }
}