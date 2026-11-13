package com.proyectofinal.controller;

import com.proyectofinal.DAO.AccesoriosDAO;
import com.proyectofinal.modelo.Accesorios;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accesorios")
@CrossOrigin(origins = "*")

public class AccesoriosController {
    private final AccesoriosDAO accesoriosDAO;

    public AccesoriosController(AccesoriosDAO accesoriosDAO) {
        this.accesoriosDAO = accesoriosDAO;
    }

    @GetMapping
    public ResponseEntity<List<Accesorios>> listar() {
        List<Accesorios> accesorios = accesoriosDAO.listar();
        if (accesorios == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(accesorios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Accesorios> consultar(@PathVariable int id) {
        Accesorios accesorio = accesoriosDAO.consultar(id);
        if (accesorio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accesorio);
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Accesorios accesorio) {
        String error = validar(accesorio);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }
        if (!accesoriosDAO.registrar(accesorio)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo registrar el accesorio");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(accesorio);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable int id, @RequestBody Accesorios accesorio) {
        if (accesoriosDAO.consultar(id) == null) {
            return ResponseEntity.notFound().build();
        }
        String error = validar(accesorio);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }
        accesorio.setIdAccesorio(id);
        if (!accesoriosDAO.modificar(accesorio)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo modificar el accesorio");
        }
        return ResponseEntity.ok(accesorio);
    }

    private String validar(Accesorios accesorio) {
        if (accesorio.getNombre() == null || accesorio.getNombre().isBlank()) {
            return "El nombre es obligatorio";
        }
        if (accesorio.getTarifaFija() == null || accesorio.getTarifaFija().compareTo(BigDecimal.ZERO) < 0) {
            return "La tarifa fija no puede ser negativa";
        }
        if (accesorio.getPrecioReposicion() == null || accesorio.getPrecioReposicion().compareTo(BigDecimal.ZERO) < 0) {
            return "El precio de reposición no puede ser negativo";
        }
        if (accesorio.getExistenciaActual() < 0) {
            return "La existencia no puede ser negativa";
        }
        return null;
    }

}
