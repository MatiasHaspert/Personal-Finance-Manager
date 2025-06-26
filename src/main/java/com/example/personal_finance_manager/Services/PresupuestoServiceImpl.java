package com.example.personal_finance_manager.Services;


import com.example.personal_finance_manager.DTOs.CategoriaMontoDTO;
import com.example.personal_finance_manager.DTOs.PresupuestoRequestDTO;
import com.example.personal_finance_manager.DTOs.PresupuestoResponseDTO;
import com.example.personal_finance_manager.Exceptions.BadRequestException;
import com.example.personal_finance_manager.Exceptions.NotFoundException;
import com.example.personal_finance_manager.Models.Presupuesto;
import com.example.personal_finance_manager.Models.TipoCategoria;
import com.example.personal_finance_manager.Models.TipoTransaccion;
import com.example.personal_finance_manager.Models.Usuario;
import com.example.personal_finance_manager.Repositories.PresupuestoRepository;
import com.example.personal_finance_manager.Repositories.TransaccionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Primary
@Transactional
@Service
public class PresupuestoServiceImpl implements PresupuestoService{

    PresupuestoRepository presupuestoRepository;
    TransaccionRepository transaccionRepository;

    @Autowired
    public PresupuestoServiceImpl(PresupuestoRepository presupuestoRepository, TransaccionRepository transaccionRepository){
        this.presupuestoRepository = presupuestoRepository;
        this.transaccionRepository = transaccionRepository;
    }

    public PresupuestoResponseDTO crearPresupuesto(Usuario usuario, PresupuestoRequestDTO presupuestoRequestDTO) {
        Presupuesto presupuesto = aPresupuestoEntity(usuario, presupuestoRequestDTO);

        if (presupuesto.getCategoria() == null) { // El usuario crea un presupuesto mensual
            if (isExistsPresupuestoMensual(usuario.getId(), presupuesto.getFecha())) {
                throw new BadRequestException("El presupuesto mensual ya existe");
            }
        }else if(isExistsPresupuestoCategoria(usuario.getId(), presupuesto.getFecha(), presupuesto.getCategoria())){
                throw new BadRequestException("El presupuesto para la categoría ya existe");
        }


        return procesarPresupuesto(presupuesto, usuario);
    }

    public PresupuestoResponseDTO actualizarPresupuesto(Usuario usuario, Long presupuestoId, PresupuestoRequestDTO presupuestoRequestDTO, LocalDate fecha) {
        Presupuesto presupuesto = aPresupuestoEntity(usuario, presupuestoRequestDTO);
        presupuesto.setId(presupuestoId);

        if(presupuesto.getCategoria() == null){ // Actualizar presupuesto mensual
            if(!isExistsPresupuestoMensualPorID(presupuestoId, usuario.getId(), fecha)){
                throw new NotFoundException("Presupuesto mensual no encontrado");
            }
        }else if(!isExistsPresupuestoDeCategoriaPorID(presupuestoId, usuario.getId(), fecha)){ // Actualizar presupuesto de categoría
                throw new NotFoundException("Presupuesto no encontrado");
        }


        return procesarPresupuesto(presupuesto, usuario);
    }

    public PresupuestoResponseDTO getPresupuestoMensual(Long id, LocalDate fecha) {

        if(!isExistsPresupuestoMensual(id, fecha)){
            throw new NotFoundException("Presupuesto mensual no encontrado.");
        }

        Presupuesto presupuesto = presupuestoRepository.findPresupuestoMensualByUsuarioAndFechaAndCategoriaIsNull(id, fecha.getYear(), fecha.getMonthValue());

        BigDecimal montoGastado = transaccionRepository.sumMontoTransaccionesByUsuarioAndTipoAndFecha(id, TipoTransaccion.GASTO.name(), fecha.getYear(), fecha.getMonthValue());

        return aPresupuestoResumenDTO(presupuesto, montoGastado, null);
    }

    public List<PresupuestoResponseDTO> getPresupuestosPorCategoria(Long id, LocalDate fecha) {
        List<Presupuesto> presupuestoCategoriaList = presupuestoRepository.findPresupuestosDeCategoriasByUsuarioAndFechaAndCategoriaisNotNull(id, fecha.getYear(), fecha.getMonthValue());
        List<CategoriaMontoDTO> categoriaMontoDTOList = presupuestoRepository.sumGastosDePresupuestoCategoriaByUsuarioAndFechaAndTipo(id, TipoTransaccion.GASTO.name(), fecha.getYear(), fecha.getMonthValue());
        List<PresupuestoResponseDTO> presupuestoResponseDTOList = new ArrayList<>();

        for (Presupuesto p : presupuestoCategoriaList) {
            BigDecimal montoGastado = BigDecimal.ZERO;

            for (CategoriaMontoDTO gasto : categoriaMontoDTOList) {
                if (gasto.getCategoria().equals(p.getCategoria().name())) {
                    montoGastado = gasto.getMonto();
                    break;
                }
            }

            presupuestoResponseDTOList.add(aPresupuestoResumenDTO(p, montoGastado, null));
        }
        return presupuestoResponseDTOList;
    }

    public PresupuestoResponseDTO procesarPresupuesto(Presupuesto presupuesto, Usuario usuario){

        // Elimina un presupuesto de categoría si se quiere actualizar un valor a 0
        if (presupuesto.getCategoria() != null && presupuesto.getMonto().compareTo(BigDecimal.ZERO) == 0) {
            // Lo interpreto como eliminación
            presupuestoRepository.deleteById(presupuesto.getId());
            return new PresupuestoResponseDTO(null, null, null, null, null, null, null, "El presupuesto fue eliminado porque el monto fue 0");
        }

        if(presupuesto.getCategoria() == null){ // Presupuesto mensual
            return procesarPresupuestoMensual(presupuesto, usuario);
        }

        // Presupuesto de categoría
        return procesarPresupuestoCategoria(presupuesto, usuario);
    }

    public boolean actualizarPresupuestoSiEsNecesario(Long usuarioId, Presupuesto nuevoPresupuesto){
        Presupuesto presupuestoMensual = presupuestoRepository.findPresupuestoMensualByUsuarioAndFechaAndCategoriaIsNull(usuarioId, nuevoPresupuesto.getFecha().getYear(), nuevoPresupuesto.getFecha().getMonthValue());

        if (presupuestoMensual == null) {
            throw new BadRequestException("No existe un presupuesto mensual para esta fecha");
        }

        BigDecimal montoTotalPresupuestado = presupuestoRepository.sumMontoTotalPresupuestadoCategoriasByUsuarioAndFecha(usuarioId, nuevoPresupuesto.getFecha().getYear(), nuevoPresupuesto.getFecha().getMonthValue());

        if(nuevoPresupuesto.getId() != null){ // Se busca actualizar un presupuesto
            BigDecimal montoViejoPresupuestado = presupuestoRepository.findMontoPresupuestoCategoriaByUsuarioAndCategoriaAndFecha(usuarioId, nuevoPresupuesto.getCategoria().name(), nuevoPresupuesto.getFecha().getYear(), nuevoPresupuesto.getFecha().getMonthValue());

            // Resto el presupuesto viejo y sumo el nuevo.
            montoTotalPresupuestado = montoTotalPresupuestado.subtract(montoViejoPresupuestado).add(nuevoPresupuesto.getMonto());
        }else{
            // Se busca crear un presupuesto, sumo el nuevo presupuesto
            montoTotalPresupuestado = montoTotalPresupuestado.add(nuevoPresupuesto.getMonto());
        }

        // Si el presupuesto mensual actual es menor que la suma total de los presupuestos de categoría actuales
        boolean seDebeActualizarPresupuestoMensual = presupuestoMensual.getMonto().compareTo(montoTotalPresupuestado) < 0;
        if(seDebeActualizarPresupuestoMensual){
            presupuestoMensual.setMonto(montoTotalPresupuestado); // Nuevo monto
            presupuestoRepository.save(presupuestoMensual); // Actualizo presupuesto mensual
        }

        return seDebeActualizarPresupuestoMensual;
    }

    public void eliminarPresupuesto(Long presupuestoId, Long usuarioId, LocalDate fecha) {
        if(!presupuestoRepository.existsPresupuestoDeCategoriaIdByIdAndUsuarioAndFecha(presupuestoId, usuarioId, fecha.getYear(), fecha.getMonthValue())){
            throw new NotFoundException("Presupuesto con id " + presupuestoId + "no encontrado.");
        }
        presupuestoRepository.deleteById(presupuestoId);
    }

    public PresupuestoResponseDTO procesarPresupuestoMensual(Presupuesto presupuesto, Usuario usuario){
        BigDecimal montoGastado;
        String mensaje = null;

        //Si se busca actualizar el presupuesto mensual con un monto menor a la suma de los presupuestos de categorías, lanzo bad request.
        BigDecimal montoTotalPresupuestoCategoria = presupuestoRepository.sumMontoTotalPresupuestadoCategoriasByUsuarioAndFecha(usuario.getId(), presupuesto.getFecha().getYear(), presupuesto.getFecha().getMonthValue());
        if(presupuesto.getMonto().compareTo(montoTotalPresupuestoCategoria) < 0){
            throw new BadRequestException("El presupuesto total no puede ser inferior a la suma de los presupuestos de las categorías: $" + montoTotalPresupuestoCategoria);
        }

        // Obtengo monto de los gastos totales del mes
        montoGastado = transaccionRepository.sumMontoTransaccionesByUsuarioAndTipoAndFecha(usuario.getId(), TipoTransaccion.GASTO.name(), presupuesto.getFecha().getYear(), presupuesto.getFecha().getMonthValue());

        return aPresupuestoResumenDTO(presupuestoRepository.save(presupuesto), montoGastado, mensaje);
    }

    public PresupuestoResponseDTO procesarPresupuestoCategoria(Presupuesto presupuesto, Usuario usuario){
        BigDecimal montoGastado;
        String mensaje = null;
        /*
        Presupuesto de categoría
        Si el usuario crea/actualiza un monto presupuestado para una categoría y
        sobrepasa al presupuestado mensual, lo actualizo.
        */
        if (actualizarPresupuestoSiEsNecesario(usuario.getId(), presupuesto)){
            mensaje = "El total de los presupuestos por categoría superó el presupuesto mensual, por lo " +
                    "que este último fue actualizado automáticamente.";
        }

        // Obtengo el monto de los gastos totales de dicha categoría
        montoGastado = transaccionRepository.sumMontoTransaccionesByCategoriaAndUsuarioAndTipoAndFecha(usuario.getId(), TipoTransaccion.GASTO.name(), presupuesto.getCategoria().name(), presupuesto.getFecha().getYear(), presupuesto.getFecha().getMonthValue());

        // El usuario crea un presupuesto de categoría
        return aPresupuestoResumenDTO(presupuestoRepository.save(presupuesto), montoGastado, mensaje);
    }
    public boolean isExistsPresupuestoMensualPorID(Long presupuestoId, Long usuarioId, LocalDate fecha) {
        return presupuestoRepository.existsPresupuestoMensualIdByIdAndUsuarioAndFecha(presupuestoId, usuarioId ,fecha.getYear(), fecha.getMonthValue());
    }

    public boolean isExistsPresupuestoDeCategoriaPorID(Long presupuestoId, Long usuarioId, LocalDate fecha){
        return presupuestoRepository.existsPresupuestoDeCategoriaIdByIdAndUsuarioAndFecha(presupuestoId, usuarioId, fecha.getYear(), fecha.getMonthValue());
    }

    public boolean isExistsPresupuestoMensual(Long id, LocalDate fecha) {
        return presupuestoRepository.existsPresupuestoMensualByUsuarioAndFecha(id, fecha.getYear(), fecha.getMonthValue());
    }

    public boolean isExistsPresupuestoCategoria(Long id, LocalDate fecha, TipoCategoria categoria){
        return presupuestoRepository.existsPresupuestoDeCategoriaByUsuarioAndFechaAndCategoria(id, fecha.getYear(), fecha.getMonthValue(), categoria.name());
    }

    public Presupuesto aPresupuestoEntity(Usuario usuario, PresupuestoRequestDTO presupuestoRequestDTO){
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setId(presupuestoRequestDTO.getId());
        presupuesto.setCategoria(presupuestoRequestDTO.getCategoria());
        presupuesto.setMonto(presupuestoRequestDTO.getMonto());
        presupuesto.setFecha(presupuestoRequestDTO.getFecha().withDayOfMonth(1)); // Asegurar primer día del mes
        presupuesto.setUsuario(usuario);
        return presupuesto;
    }

    public PresupuestoResponseDTO aPresupuestoResumenDTO(Presupuesto presupuesto, BigDecimal montoGastado, String mensaje) {
        PresupuestoResponseDTO presupuestoResponseDTO = new PresupuestoResponseDTO();

        presupuestoResponseDTO.setId(presupuesto.getId());
        presupuestoResponseDTO.setFecha(presupuesto.getFecha());
        presupuestoResponseDTO.setMontoGastos(montoGastado);
        presupuestoResponseDTO.setMontoPresupuestado(presupuesto.getMonto());
        presupuestoResponseDTO.setCategoria(presupuesto.getCategoria());

        // Calcular porcentaje ejecutado
        BigDecimal porcentajeEjecutado = BigDecimal.ZERO;
        if (presupuesto.getMonto().compareTo(BigDecimal.ZERO) > 0) {
            porcentajeEjecutado = montoGastado
                    .multiply(BigDecimal.valueOf(100))
                    .divide(presupuesto.getMonto(), 2, RoundingMode.HALF_UP);
        }

        presupuestoResponseDTO.setPorcentajeEjecutado(porcentajeEjecutado);

        // Calcular monto restante
        BigDecimal montoRestante = presupuesto.getMonto().subtract(montoGastado);
        presupuestoResponseDTO.setMontoRestante(montoRestante);

        if (montoRestante.compareTo(BigDecimal.ZERO) < 0 && mensaje == null) {
            mensaje = (presupuesto.getCategoria() == null)
                    ? "Sus gastos han excedido su presupuesto total"
                    : "Sus gastos han excedido su presupuesto de categoría";
        }

        presupuestoResponseDTO.setMensaje(mensaje);

        return presupuestoResponseDTO;
    }
}
