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

    PresupuestoResponseDTO getPresupuestoMensual(Usuario usuario, LocalDate fecha);

    PresupuestoResponseDTO crearPresupuesto(Usuario usuario, PresupuestoRequestDTO presupuestoRequestDTO);

    List<PresupuestoResponseDTO> getPresupuestosPorCategoria(Usuario usuario, LocalDate fecha);

    PresupuestoResponseDTO actualizarPresupuesto(Usuario usuario, Long presupuestoId, PresupuestoRequestDTO presupuesto, LocalDate fecha);

    boolean isExistsPresupuestoMensualPorUsuarioId(Usuario usuario, LocalDate fecha);

    boolean isExistsPresupuestoCategoriaPorUsuarioId(Usuario usuario, LocalDate fecha, TipoCategoria categoria);

    boolean isExistsPresupuestoMensualPorIDyUsuarioId(Long presupuestoId, Usuario usuario, LocalDate fecha);

    boolean isExistsPresupuestoDeCategoriaPorIDyUsuarioId(Long presupuestoID, Usuario usuario, LocalDate fecha);

    PresupuestoResponseDTO procesarPresupuestoMensual(Presupuesto presupuesto, Usuario usuario);

    PresupuestoResponseDTO procesarPresupuestoCategoria(Presupuesto presupuesto, Usuario usuario);

    void eliminarPresupuesto(Long presupuestoId, Usuario usuario, LocalDate fecha);

    Presupuesto aPresupuestoEntity(Usuario usuario, PresupuestoRequestDTO presupuesto);

    boolean actualizarPresupuestoSiEsNecesario(Usuario usuario, Presupuesto nuevoPresupuesto);

    PresupuestoResponseDTO aPresupuestoResumenDTO(Presupuesto presupuesto, BigDecimal montoGastado, String mensaje);

    PresupuestoResponseDTO procesarPresupuesto(Presupuesto presupuesto, Usuario usuario);
}
