package com.example.personal_finance_manager.Models;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Usuarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuario_email",
                                                columnNames = {"email"}))
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UsuarioId", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(value = EnumType.STRING)
    private Rol rol;

    @OneToMany(mappedBy = "usuario")
    private List<Transaccion> transaccionesList = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<Presupuesto> presupuestoList = new ArrayList<>();
}
