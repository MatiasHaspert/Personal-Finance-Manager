package com.example.personal_finance_manager.DTOs;

import com.example.personal_finance_manager.Models.TipoCategoria;
import com.example.personal_finance_manager.Models.TipoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransaccionResponseDTO {
    private Long id;
    private BigDecimal monto;
    private LocalDate fecha;
    private String descripcion;
    private TipoCategoria categoria;
    private TipoTransaccion tipoTransaccion;
}
