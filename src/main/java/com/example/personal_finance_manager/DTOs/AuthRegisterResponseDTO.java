package com.example.personal_finance_manager.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRegisterResponseDTO {

    String token;
    UsuarioResponseDTO usuarioResponseDTO;
}
