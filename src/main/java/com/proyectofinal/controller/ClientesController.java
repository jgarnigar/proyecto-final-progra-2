package com.proyectofinal.controller;

import java.math.BigDecimal;
import java.util.List;

import com.proyectofinal.service.ClientesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyectofinal.DAO.ClientesDAO;
import com.proyectofinal.modelo.Clientes;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClientesController {
    private final ClientesDAO clientesDAO;
    private final ClientesService clientesService;

    public ClientesController(ClientesDAO clientesDAO, ClientesService clientesService){
        this.clientesDAO = clientesDAO;
        this.clientesService = clientesService;
    }

    @GetMapping
    public ResponseEntity<List<Clientes>> listar() {
        List<Clientes> clientes = clientesDAO.listar();
        if (clientes == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clientes> consultar(@PathVariable int id) {
        Clientes cliente = clientesDAO.consultar(id);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Clientes cliente) {
        try {
            Clientes nuevoCliente = clientesService.registrarCliente(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable int id, @RequestBody Clientes cliente) {
        try {
            Clientes clienteActualizado = clientesService.modificarCliente(id, cliente);
            return ResponseEntity.ok(clienteActualizado);

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No se encontró")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }

            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivar(@PathVariable int id) {
        Clientes cliente = clientesDAO.consultar(id);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        cliente.setEstado("INACTIVO");
        if (!clientesDAO.modificar(cliente)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo desactivar el cliente");
        }
        return ResponseEntity.noContent().build();
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }

}
