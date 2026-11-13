package com.proyectofinal.controller;

import com.proyectofinal.DAO.AccesoriosDAO;
import com.proyectofinal.modelo.Accesorios;
import com.proyectofinal.service.AccesoriosService;
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
    private final AccesoriosService accesoriosService;

    public AccesoriosController(AccesoriosDAO accesoriosDAO, AccesoriosService accesoriosService) {
        this.accesoriosDAO = accesoriosDAO;
        this.accesoriosService = accesoriosService;
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
        try {
            Accesorios nuevoAccesorio = accesoriosService.registrarAccesorio(accesorio);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAccesorio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable int id, @RequestBody Accesorios accesorio) {
        try {
            Accesorios actualizado = accesoriosService.modificarAccesorio(id, accesorio);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No se encontró")) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivar(@PathVariable int id) {
        Accesorios accesorio = accesoriosDAO.consultar(id);
        if (accesorio == null) return ResponseEntity.notFound().build();

        accesorio.setExistenciaActual(0);
        if (!accesoriosDAO.modificar(accesorio)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo vaciar el stock del accesorio");
        }
        return ResponseEntity.noContent().build();
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
