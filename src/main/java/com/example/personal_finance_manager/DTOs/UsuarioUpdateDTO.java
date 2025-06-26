package com.example.personal_finance_manager.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioUpdateDTO {
    @NotBlank
    private String nombre;

    private String nuevaPassword; // opcional

    private String passwordActual; // requerido si cambia la password
}
