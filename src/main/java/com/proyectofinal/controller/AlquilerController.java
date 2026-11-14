package com.proyectofinal.controller;

import com.proyectofinal.modelo.Alquileres;
import com.proyectofinal.service.AlquilerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Importación necesaria para el método GET

@RestController
@RequestMapping("/api/alquileres")
public class AlquilerController {

    private final AlquilerService alquilerService;

    public AlquilerController(AlquilerService alquilerService) {
        this.alquilerService = alquilerService;
    }

    @GetMapping
    public List<Alquileres> obtenerTodosLosAlquileres() {
        return alquilerService.obtenerTodos();
    }

    @PostMapping
    public ResponseEntity<?> procesarNuevoAlquiler(@RequestBody Alquileres alquiler) {
        try {
            Alquileres nuevoAlquiler = alquilerService.procesarNuevoAlquiler(alquiler);
            return new ResponseEntity<>(nuevoAlquiler, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/entregar")
    public ResponseEntity<?> entregarAlquiler(@PathVariable("id") int idAlquiler) {
        try {
            Alquileres alquilerEntregado = alquilerService.entregarAlquiler(idAlquiler);
            return ResponseEntity.ok(alquilerEntregado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<?> devolverAlquiler(@PathVariable("id") int idAlquiler, @RequestBody Alquileres datosDevolucion) {
        try {
            Alquileres alquilerDevuelto = alquilerService.devolverAlquiler(idAlquiler, datosDevolucion);
            return ResponseEntity.ok(alquilerDevuelto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}