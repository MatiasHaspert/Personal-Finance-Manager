package com.example.personal_finance_manager.Models;


import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Transacciones")
@Entity
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransaccionId", nullable = false)
    private Long id;

    private BigDecimal monto;

    private LocalDate fecha;

    private String descripcion;

    @Enumerated(value = EnumType.STRING)
    private TipoCategoria categoria;

    @ManyToOne
    @JoinColumn(name = "UsuarioId")
    private Usuario usuario;

    @Enumerated(value = EnumType.STRING)
    private TipoTransaccion tipoTransaccion;
}
