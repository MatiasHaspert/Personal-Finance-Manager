package com.example.personal_finance_manager.Services;

import com.example.personal_finance_manager.DTOs.CategoriaMontoDTO;
import com.example.personal_finance_manager.DTOs.TransaccionRequestDTO;
import com.example.personal_finance_manager.DTOs.TransaccionResponseDTO;
import com.example.personal_finance_manager.Exceptions.NotFoundException;
import com.example.personal_finance_manager.Models.TipoTransaccion;
import com.example.personal_finance_manager.Models.Transaccion;
import com.example.personal_finance_manager.Models.Usuario;
import com.example.personal_finance_manager.Repositories.TransaccionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Primary
@Service
@Transactional
public class TransaccionServiceImpl implements TransaccionService{

    private TransaccionRepository transaccionRepository;

    @Autowired
    public TransaccionServiceImpl(TransaccionRepository transaccionRepository){
        this.transaccionRepository = transaccionRepository;
    }

    public Page<TransaccionResponseDTO> getTransacciones(Long id, LocalDate fecha, int page, int size) {
        // Retorna la primera página (0 default) con (10 default) elementos, ordenada por el campo fecha en orden descendente.
        Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        Page<Transaccion> transacciones = transaccionRepository.findTransaccionesPaginadasByUsuarioAndFecha(id, fecha.getYear(), fecha.getMonthValue(),  pageable);

        return transacciones.map(this::aTransaccionResponseDTO);
    }

    public TransaccionResponseDTO crearTransaccion(TransaccionRequestDTO transaccionRequestDTO, Usuario usuario) {
        transaccionRequestDTO.setUsuario(usuario);
        return aTransaccionResponseDTO(transaccionRepository.save(aTransaccionEntity(transaccionRequestDTO)));
    }

    public TransaccionResponseDTO actualizarTransaccion(Long id, TransaccionRequestDTO transaccionRequestDTO, Usuario usuario) {
        if(!transaccionRepository.existsById(id)){
            throw new NotFoundException("Transaccion no encontrada");
        }
        transaccionRequestDTO.setId(id);
        transaccionRequestDTO.setUsuario(usuario);
        return aTransaccionResponseDTO(transaccionRepository.save(aTransaccionEntity(transaccionRequestDTO)));
    }

    public void eliminarTransaccion(Long id, Usuario usuario) {
        if(!transaccionRepository.existsById(id)){
            throw new NotFoundException("Transaccion no encontrada");
        }
        transaccionRepository.deleteTransaccionByIdAndUsuario(id,usuario);
    }

    public List<TransaccionResponseDTO> getTransacionesPorTipo(Long id, TipoTransaccion tipoTransaccion, LocalDate fecha) {
        List<Transaccion> transacciones = transaccionRepository.findTransaccionesByUsuarioAndTipoAndFecha(id, tipoTransaccion.name(), fecha.getYear(), fecha.getMonthValue());

        return transacciones.stream().map(this::aTransaccionResponseDTO).toList();
    }


    public BigDecimal getSaldoMensual(Long id, LocalDate fecha) {
        BigDecimal ingresosTotales, gastosTotales;

        ingresosTotales = getMontoTotalMensualPorTipo(id, TipoTransaccion.INGRESO, fecha);
        gastosTotales = getMontoTotalMensualPorTipo(id, TipoTransaccion.GASTO, fecha);

        return ingresosTotales.subtract(gastosTotales);
    }

    public List<CategoriaMontoDTO> getCategoriaMontoTotalPorTipo(Long id, TipoTransaccion tipo, LocalDate fecha) {
        return transaccionRepository.getCategoriaMontoByUsuarioAndTipoAndFecha(id, tipo.name(), fecha.getYear(), fecha.getMonthValue());
    }

    public BigDecimal getMontoTotalMensualPorTipo(Long id, TipoTransaccion tipoTransaccion, LocalDate fecha) {
        return transaccionRepository.sumMontoTransaccionesByUsuarioAndTipoAndFecha(id, tipoTransaccion.name(), fecha.getYear(), fecha.getMonthValue());
    }

    public Transaccion aTransaccionEntity(TransaccionRequestDTO transaccionRequestDTO) {
        Transaccion transaccion = new Transaccion();

        transaccion.setId(transaccionRequestDTO.getId());
        transaccion.setUsuario(transaccionRequestDTO.getUsuario());
        transaccion.setTipoTransaccion(transaccionRequestDTO.getTipoTransaccion());
        transaccion.setFecha(transaccionRequestDTO.getFecha());
        transaccion.setDescripcion(transaccionRequestDTO.getDescripcion());
        transaccion.setMonto(transaccionRequestDTO.getMonto());
        transaccion.setCategoria(transaccionRequestDTO.getCategoria());
        return transaccion;
    }

    public TransaccionResponseDTO aTransaccionResponseDTO(Transaccion transaccion){
        TransaccionResponseDTO transaccionResponseDTO = new TransaccionResponseDTO();

        transaccionResponseDTO.setId(transaccion.getId());
        transaccionResponseDTO.setDescripcion(transaccion.getDescripcion());
        transaccionResponseDTO.setTipoTransaccion(transaccion.getTipoTransaccion());
        transaccionResponseDTO.setMonto(transaccion.getMonto());
        transaccionResponseDTO.setCategoria(transaccion.getCategoria());
        transaccionResponseDTO.setFecha(transaccion.getFecha());

        return transaccionResponseDTO;
    }
}
