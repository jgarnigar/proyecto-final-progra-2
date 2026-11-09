package com.proyectofinal.Controller;

import com.proyectofinal.DAO.EquiposDAO;
import com.proyectofinal.modelo.Equipos;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/equipos")
@CrossOrigin(origins = "*")

public class EquiposController {
    private final EquiposDAO equiposDAO;

    public EquiposController(EquiposDAO equiposDAO) {
        this.equiposDAO = equiposDAO;
    }

    @GetMapping
    public ResponseEntity<List<Equipos>> listar() {
        List<Equipos> equipos = equiposDAO.listar();
        if (equipos == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(equipos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipos> consultar(@PathVariable int id) {
        Equipos equipo = equiposDAO.consultar(id);
        if (equipo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(equipo);
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Equipos equipo) {
        String error = validar(equipo);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }
        if (equipo.getEstado() == null || equipo.getEstado().isBlank()) {
            equipo.setEstado("DISPONIBLE");
        }
        if (!equiposDAO.registrar(equipo)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo registrar el equipo");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(equipo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable int id, @RequestBody Equipos equipo) {
        if (equiposDAO.consultar(id) == null) {
            return ResponseEntity.notFound().build();
        }
        String error = validar(equipo);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }
        equipo.setIdEquipo(id);
        if (equipo.getEstado() == null || equipo.getEstado().isBlank()) {
            equipo.setEstado("DISPONIBLE");
        }
        if (!equiposDAO.modificar(equipo)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo modificar el equipo");
        }
        return ResponseEntity.ok(equipo);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivar(@PathVariable int id) {
        Equipos equipo = equiposDAO.consultar(id);
        if (equipo == null) {
            return ResponseEntity.notFound().build();
        }
        equipo.setEstado("INACTIVO");
        if (!equiposDAO.modificar(equipo)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo desactivar el equipo");
        }
        return ResponseEntity.noContent().build();
    }

    private String validar(Equipos equipo) {
        if (equipo.getTipo() == null || equipo.getTipo().isBlank()
                || equipo.getModelo() == null || equipo.getModelo().isBlank()) {
            return "El tipo y el modelo son obligatorios";
        }
        if (equipo.getTarifaHora() == null || equipo.getTarifaHora().compareTo(BigDecimal.ZERO) <= 0) {
            return "La tarifa por hora debe ser mayor que 0";
        }
        if (equipo.getPrecioReposicion() == null || equipo.getPrecioReposicion().compareTo(BigDecimal.ZERO) <= 0) {
            return "El precio de reposición debe ser mayor que 0";
        }
        if (equipo.getBateriaPorcentaje() < 0 || equipo.getBateriaPorcentaje() > 100) {
            return "El porcentaje de batería debe estar entre 0 y 100";
        }
        return null;
    }
}
