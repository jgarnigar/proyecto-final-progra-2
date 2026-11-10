package com.proyectofinal.Controller;

import java.math.BigDecimal;
import java.util.List;

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

    public ClientesController(ClientesDAO clientesDAO){
        this.clientesDAO = clientesDAO;
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
        if (esVacio(cliente.getNombre()) || esVacio(cliente.getEmail())) {
            return ResponseEntity.badRequest().body("El nombre y el email son obligatorios");
        }
        if (cliente.getSaldoPendiente() == null) {
            cliente.setSaldoPendiente(BigDecimal.ZERO);
        }
        if (esVacio(cliente.getEstado())) {
            cliente.setEstado("ACTIVO");
        }
        if (!clientesDAO.registrar(cliente)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo registrar el cliente");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable int id, @RequestBody Clientes cliente) {
        if (clientesDAO.consultar(id) == null) {
            return ResponseEntity.notFound().build();
        }
        if (esVacio(cliente.getNombre()) || esVacio(cliente.getEmail())) {
            return ResponseEntity.badRequest().body("El nombre y el email son obligatorios");
        }
        cliente.setIdCliente(id);
        if (cliente.getSaldoPendiente() == null) {
            cliente.setSaldoPendiente(BigDecimal.ZERO);
        }
        if (esVacio(cliente.getEstado())) {
            cliente.setEstado("ACTIVO");
        }
        if (!clientesDAO.modificar(cliente)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo modificar el cliente");
        }
        return ResponseEntity.ok(cliente);
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
