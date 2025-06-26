package com.example.personal_finance_manager.Controllers;

import com.example.personal_finance_manager.DTOs.PresupuestoRequestDTO;
import com.example.personal_finance_manager.Models.Usuario;
import com.example.personal_finance_manager.Services.AuthService;
import com.example.personal_finance_manager.Services.PresupuestoService;
import com.example.personal_finance_manager.Services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/presupuestos")
public class PresupuestoController {

    private PresupuestoService presupuestoService;
    private AuthService authService;

    @Autowired
    public PresupuestoController(PresupuestoService presupuestoService, AuthService authService){
        this.presupuestoService = presupuestoService;
        this.authService = authService;
    }

    @GetMapping("/mensual")
    public ResponseEntity<?> getPresupuestoMensual(
            @RequestParam String email,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
            ){
        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        return ResponseEntity.ok(presupuestoService.getPresupuestoMensual(usuario.getId(), fecha));
    }

    @PostMapping
    public ResponseEntity<?> crearPresupuesto(
            @RequestParam String email,
            @Valid @RequestBody PresupuestoRequestDTO presupuesto
    ){
        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.crearPresupuesto(usuario, presupuesto));
    }

    @GetMapping("/categoria")
    public ResponseEntity<?> getPresupuestosCategoria(
            @RequestParam String email,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ){
        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        return ResponseEntity.ok(presupuestoService.getPresupuestosPorCategoria(usuario.getId(), fecha));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPresupuesto(
            @PathVariable Long id,
            @RequestParam String email,
            @Valid @RequestBody PresupuestoRequestDTO presupuesto,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ){
        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        return ResponseEntity.ok(presupuestoService.actualizarPresupuesto(usuario,id, presupuesto, fecha));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPresupuesto(
            @PathVariable Long id,
            @RequestParam String email,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ){
        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        presupuestoService.eliminarPresupuesto(id, usuario.getId(), fecha);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
