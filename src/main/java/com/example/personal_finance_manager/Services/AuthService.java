package com.example.personal_finance_manager.Services;


import com.example.personal_finance_manager.DTOs.AuthRegisterResponseDTO;
import com.example.personal_finance_manager.DTOs.AuthRequestDTO;
import com.example.personal_finance_manager.DTOs.UsuarioRequestDTO;
import com.example.personal_finance_manager.Models.Usuario;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthRegisterResponseDTO login(AuthRequestDTO request);
    AuthRegisterResponseDTO register(UsuarioRequestDTO request);
    Usuario obtenerUsuarioAutenticado(Authentication authentication);
}
