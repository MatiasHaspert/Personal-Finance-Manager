package com.example.personal_finance_manager.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaMontoDTO {
    private String categoria; // Categoría
    private BigDecimal monto; // Monto asociado (ya sea gastos o ingresos)
}
