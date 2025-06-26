package com.example.personal_finance_manager.Services;


import com.example.personal_finance_manager.DTOs.PresupuestoRequestDTO;
import com.example.personal_finance_manager.DTOs.PresupuestoResponseDTO;
import com.example.personal_finance_manager.Models.Presupuesto;
import com.example.personal_finance_manager.Models.TipoCategoria;
import com.example.personal_finance_manager.Models.Usuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PresupuestoService {

    PresupuestoResponseDTO getPresupuestoMensual(Long id, LocalDate fecha);

    PresupuestoResponseDTO crearPresupuesto(Usuario usuario, PresupuestoRequestDTO presupuestoRequestDTO);

    List<PresupuestoResponseDTO> getPresupuestosPorCategoria(Long id, LocalDate fecha);

    PresupuestoResponseDTO actualizarPresupuesto(Usuario usuario, Long presupuestoId, PresupuestoRequestDTO presupuesto, LocalDate fecha);

    boolean isExistsPresupuestoMensual(Long id, LocalDate fecha);

    boolean isExistsPresupuestoCategoria(Long id, LocalDate fecha, TipoCategoria categoria);

    boolean isExistsPresupuestoMensualPorID(Long presupuestoId, Long id, LocalDate fecha);

    boolean isExistsPresupuestoDeCategoriaPorID(Long presupuestoID, Long id, LocalDate fecha);

    PresupuestoResponseDTO procesarPresupuestoMensual(Presupuesto presupuesto, Usuario usuario);

    PresupuestoResponseDTO procesarPresupuestoCategoria(Presupuesto presupuesto, Usuario usuario);

    void eliminarPresupuesto(Long presupuestoId, Long usuarioId, LocalDate fecha);

    Presupuesto aPresupuestoEntity(Usuario usuario, PresupuestoRequestDTO presupuesto);

    boolean actualizarPresupuestoSiEsNecesario(Long usuarioId, Presupuesto nuevoPresupuesto);

    PresupuestoResponseDTO aPresupuestoResumenDTO(Presupuesto presupuesto, BigDecimal montoGastado, String mensaje);

    PresupuestoResponseDTO procesarPresupuesto(Presupuesto presupuesto, Usuario usuario);
}
