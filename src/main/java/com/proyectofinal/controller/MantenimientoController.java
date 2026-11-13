package com.proyectofinal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyectofinal.modelo.Mantenimientos;
import com.proyectofinal.service.InventarioService;

@RestController
@RequestMapping("/api/mantenimientos")
@CrossOrigin(origins = "*")

public class MantenimientoController {   
     private final InventarioService inventarioService;

    public MantenimientoController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Mantenimientos mantenimiento) {
        try {
            Mantenimientos registrado = inventarioService.registrarMantenimiento(mantenimiento);
            return ResponseEntity.status(HttpStatus.CREATED).body(registrado);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/equipo/{idEquipo}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable int idEquipo) {
        try {
            List<Mantenimientos> historial = inventarioService.obtenerHistorial(idEquipo);
            return ResponseEntity.ok(historial);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/cerrar/{idEquipo}")
    public ResponseEntity<?> cerrar(@PathVariable int idEquipo) {
        try {
            inventarioService.cerrarMantenimiento(idEquipo);

            return ResponseEntity.ok( "Mantenimiento cerrado. El equipo está disponible." );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

