package com.example.personal_finance_manager.DTOs;

import com.example.personal_finance_manager.Models.TipoCategoria;
import com.example.personal_finance_manager.Models.TipoTransaccion;
import com.example.personal_finance_manager.Models.Usuario;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransaccionRequestDTO {

    private Long id;

    @NotNull(message = "Monto obligatorio")
    @DecimalMax(value = "9999999999.0")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotNull(message = "Fecha obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @Size(max = 200, message = "La descripción no puede tener más de 200 caracteres")
    private String descripcion;

    @NotNull(message = "Categoría obligatoria")
    private TipoCategoria categoria;

    private Usuario usuario;

    @NotNull(message = "Tipo de transacción es obligatorio")
    private TipoTransaccion tipoTransaccion;
}
