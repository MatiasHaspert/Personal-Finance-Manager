package com.example.personal_finance_manager.Services;

import com.example.personal_finance_manager.DTOs.CategoriaMontoDTO;
import com.example.personal_finance_manager.DTOs.TransaccionRequestDTO;
import com.example.personal_finance_manager.DTOs.TransaccionResponseDTO;
import com.example.personal_finance_manager.Models.TipoTransaccion;
import com.example.personal_finance_manager.Models.Transaccion;
import com.example.personal_finance_manager.Models.Usuario;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransaccionService {

    TransaccionResponseDTO crearTransaccion(TransaccionRequestDTO transaccionRequestDTO, Usuario usuario);

    Page<TransaccionResponseDTO> getTransacciones(Long id, LocalDate fecha, int page, int size);

    TransaccionResponseDTO actualizarTransaccion(Long id, TransaccionRequestDTO transaccionRequestDTO, Usuario usuario);

    void eliminarTransaccion(Long id, Usuario usuario);

    List<TransaccionResponseDTO> getTransacionesPorTipo(Long id, TipoTransaccion tipoTransaccion, LocalDate fecha);

    BigDecimal getSaldoMensual(Long id, LocalDate fecha);

    BigDecimal getMontoTotalMensualPorTipo(Long id, TipoTransaccion tipoTransaccion, LocalDate fecha);

    List<CategoriaMontoDTO> getCategoriaMontoTotalPorTipo(Long id, TipoTransaccion tipo, LocalDate fecha);

    Transaccion aTransaccionEntity(TransaccionRequestDTO transaccionRequestDTO);

    TransaccionResponseDTO aTransaccionResponseDTO(Transaccion transaccion);
}
