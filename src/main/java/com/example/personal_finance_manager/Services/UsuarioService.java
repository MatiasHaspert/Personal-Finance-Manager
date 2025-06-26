package com.example.personal_finance_manager.Services;

import com.example.personal_finance_manager.DTOs.UsuarioRequestDTO;
import com.example.personal_finance_manager.DTOs.UsuarioResponseDTO;
import com.example.personal_finance_manager.DTOs.UsuarioUpdateDTO;
import com.example.personal_finance_manager.Models.Usuario;
import com.example.personal_finance_manager.Security.UserDetailsImpl;
import org.springframework.security.core.Authentication;

public interface UsuarioService {
    Usuario obtenerUsuarioPorEmail(String email);

    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO usuarioRequestDTO);

    UsuarioResponseDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto);

    Usuario aUsuarioEntity(UsuarioRequestDTO usuarioRequestDTO);

    UsuarioResponseDTO aUsuarioResponseDTO(Usuario usuario);

    UsuarioResponseDTO aUsuarioResponseDTOFromUserDetails(UserDetailsImpl userDetails);
}
