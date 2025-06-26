package com.example.personal_finance_manager.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Table(name = "Presupuestos")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PresupuestoId", nullable = false)
    private Long id;

    LocalDate fecha;

    BigDecimal monto;

    @ManyToOne
    @JoinColumn(name = "UsuarioId")
    private Usuario usuario;

    @Enumerated(value = EnumType.STRING)
    private TipoCategoria categoria;
}
