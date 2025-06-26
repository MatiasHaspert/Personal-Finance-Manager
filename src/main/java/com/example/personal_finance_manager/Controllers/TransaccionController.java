package com.example.personal_finance_manager.Controllers;

import com.example.personal_finance_manager.DTOs.CategoriaMontoDTO;
import com.example.personal_finance_manager.DTOs.PaginacionResponseDTO;
import com.example.personal_finance_manager.DTOs.TransaccionRequestDTO;
import com.example.personal_finance_manager.DTOs.TransaccionResponseDTO;
import com.example.personal_finance_manager.Models.TipoTransaccion;
import com.example.personal_finance_manager.Models.Usuario;
import com.example.personal_finance_manager.Security.UserDetailsImpl;
import com.example.personal_finance_manager.Services.AuthService;
import com.example.personal_finance_manager.Services.TransaccionService;
import com.example.personal_finance_manager.Services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@RequestMapping("/transacciones")
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class TransaccionController {

    private TransaccionService transaccionService;
    private AuthService authService;

    @Autowired
    public TransaccionController(TransaccionService transaccionService, AuthService authService) {
        this.transaccionService = transaccionService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<?> crearTransaccion(
            @Valid @RequestBody TransaccionRequestDTO transaccion,
            Authentication authentication
    ) {
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaccionService.crearTransaccion(transaccion, usuario));
    }

    @GetMapping
    public ResponseEntity<?> getTransacciones(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso  = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);

        Page<TransaccionResponseDTO> transaccionPage = transaccionService.getTransacciones(usuario.getId(), fecha, page, size);

        PaginacionResponseDTO<TransaccionResponseDTO> response = new PaginacionResponseDTO<>();

        response.setContenido(transaccionPage.getContent());
        response.setPaginaActual(transaccionPage.getNumber());
        response.setTotalPaginas(transaccionPage.getTotalPages());
        response.setTotalElementos(transaccionPage.getTotalElements());
        response.setUltimaPagina(transaccionPage.isLast());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransaccionResponseDTO> actualizarTransaccion(
            @PathVariable Long id,
            @Valid @RequestBody TransaccionRequestDTO transaccion,
            Authentication authentication
    ) {
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);

        return ResponseEntity.ok(transaccionService.actualizarTransaccion(id, transaccion, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarTransaccion(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);

        transaccionService.eliminarTransaccion(id, usuario);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/por-tipo")
    public ResponseEntity<List<TransaccionResponseDTO>> getTransaccionesPorTipo(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam TipoTransaccion tipo
    ) {
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);
        return ResponseEntity.ok(transaccionService.getTransacionesPorTipo(usuario.getId(), tipo, fecha));
    }


    @GetMapping("/saldo")
    public ResponseEntity<BigDecimal> getSaldoMensual(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);

        return ResponseEntity.ok(transaccionService.getSaldoMensual(usuario.getId(), fecha));
    }

    @GetMapping("/monto-categoria-por-tipo")
    public ResponseEntity<List<CategoriaMontoDTO>> getMontoCategoriaPorTipo(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam TipoTransaccion tipo
    ) {
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);

        return ResponseEntity.ok(transaccionService.getCategoriaMontoTotalPorTipo(usuario.getId(), tipo, fecha));
    }

    @GetMapping("/tipo-monto-total")
    public ResponseEntity<BigDecimal> getMontoTotalPorTipo(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam TipoTransaccion tipo
    ) {
        Usuario usuario = authService.obtenerUsuarioAutenticado(authentication);
        return ResponseEntity.ok(transaccionService.getMontoTotalMensualPorTipo(usuario.getId(), tipo, fecha));
    }
}