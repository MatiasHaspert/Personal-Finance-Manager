package com.example.personal_finance_manager.Controllers;

import com.example.personal_finance_manager.DTOs.PresupuestoRequestDTO;
import com.example.personal_finance_manager.DTOs.PresupuestoResponseDTO;
import com.example.personal_finance_manager.Models.Usuario;
import com.example.personal_finance_manager.Services.AuthService;
import com.example.personal_finance_manager.Services.PresupuestoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
    public ResponseEntity<PresupuestoResponseDTO> getPresupuestoMensual(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
            ){
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);

        return ResponseEntity.ok(presupuestoService.getPresupuestoMensual(usuario, fecha));
    }

    @PostMapping
    public ResponseEntity<PresupuestoResponseDTO> crearPresupuesto(
            Authentication authentication,
            @Valid @RequestBody PresupuestoRequestDTO presupuesto
    ){
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);

        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.crearPresupuesto(usuario, presupuesto));
    }

    @GetMapping("/mensual/categorias")
    public ResponseEntity<List<PresupuestoResponseDTO>> getPresupuestosCategoria(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ){
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);

        return ResponseEntity.ok(presupuestoService.getPresupuestosPorCategoria(usuario, fecha));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPresupuesto(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody PresupuestoRequestDTO presupuesto,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ){
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);

        return ResponseEntity.ok(presupuestoService.actualizarPresupuesto(usuario,id, presupuesto, fecha));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPresupuesto(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ){
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);

        presupuestoService.eliminarPresupuesto(id, usuario, fecha);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
