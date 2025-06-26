package com.example.personal_finance_manager.Controllers;

import com.example.personal_finance_manager.DTOs.*;
import com.example.personal_finance_manager.Security.UserDetailsImpl;
import com.example.personal_finance_manager.Services.AuthService;
import com.example.personal_finance_manager.Services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioAutenticado(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(usuarioService.aUsuarioResponseDTOFromUserDetails(userDetails));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> actualizarDatosDelUsuario(
            @Valid @RequestBody UsuarioUpdateDTO dto,
            Authentication authentication
    ) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UsuarioResponseDTO actualizado = usuarioService.actualizarUsuario(userDetails.getId(), dto);
        return ResponseEntity.ok(actualizado);
    }
}

