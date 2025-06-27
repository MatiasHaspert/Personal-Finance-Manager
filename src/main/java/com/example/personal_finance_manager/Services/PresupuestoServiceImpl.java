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

    private PresupuestoRepository presupuestoRepository;
    private TransaccionRepository transaccionRepository;

    @Autowired
    public PresupuestoServiceImpl(PresupuestoRepository presupuestoRepository, TransaccionRepository transaccionRepository){
        this.presupuestoRepository = presupuestoRepository;
        this.transaccionRepository = transaccionRepository;
    }

    public PresupuestoResponseDTO crearPresupuesto(Usuario usuario, PresupuestoRequestDTO presupuestoRequestDTO) {
        Presupuesto presupuesto = aPresupuestoEntity(usuario, presupuestoRequestDTO);

        if (presupuesto.getCategoria() == null) { // El usuario crea un presupuesto mensual
            if (isExistsPresupuestoMensualPorUsuarioId(usuario, presupuesto.getFecha())) {
                throw new BadRequestException("El presupuesto mensual ya existe");
            }
        }else if(isExistsPresupuestoCategoriaPorUsuarioId(usuario, presupuesto.getFecha(), presupuesto.getCategoria())){
                throw new BadRequestException("El presupuesto para la categoría ya existe");
        }


        return procesarPresupuesto(presupuesto, usuario);
    }

    public PresupuestoResponseDTO actualizarPresupuesto(Usuario usuario, Long presupuestoId, PresupuestoRequestDTO presupuestoRequestDTO, LocalDate fecha) {
        Presupuesto presupuesto = aPresupuestoEntity(usuario, presupuestoRequestDTO);
        presupuesto.setId(presupuestoId);

        if(presupuesto.getCategoria() == null){ // Actualizar presupuesto mensual
            if(!isExistsPresupuestoMensualPorIDyUsuarioId(presupuestoId, usuario, fecha)){
                throw new NotFoundException("Presupuesto mensual no encontrado");
            }
        }else if(!isExistsPresupuestoDeCategoriaPorIDyUsuarioId(presupuestoId, usuario, fecha)){ // Actualizar presupuesto de categoría
                throw new NotFoundException("Presupuesto no encontrado");
        }


        return procesarPresupuesto(presupuesto, usuario);
    }

    public void eliminarPresupuesto(Long presupuestoId, Usuario usuario, LocalDate fecha) {
        if(!presupuestoRepository.existsPresupuestoCategoriaByIdAndUsuarioAndFechaAndCategoriaIsNotNull(presupuestoId, usuario, fecha)){
            throw new NotFoundException("Presupuesto con id " + presupuestoId + "no encontrado.");
        }
        presupuestoRepository.deleteByIdAndUsuario(presupuestoId, usuario);
    }

    public PresupuestoResponseDTO getPresupuestoMensual(Usuario usuario, LocalDate fecha) {

        if(!isExistsPresupuestoMensualPorUsuarioId(usuario, fecha)){
            throw new NotFoundException("Presupuesto mensual no encontrado.");
        }

        Presupuesto presupuesto = presupuestoRepository.findPresupuestoMensualByUsuarioAndFechaAndCategoriaIsNull(usuario, fecha);

        BigDecimal montoGastado = transaccionRepository.sumMontoTransaccionesByUsuarioAndTipoAndFecha(usuario.getId(), TipoTransaccion.GASTO, fecha.getYear(), fecha.getMonthValue());

        return aPresupuestoResumenDTO(presupuesto, montoGastado, null);
    }

    public List<PresupuestoResponseDTO> getPresupuestosPorCategoria(Usuario usuario, LocalDate fecha) {
        List<Presupuesto> presupuestoCategoriaList = presupuestoRepository.findPresupuestosCategoriasByUsuarioAndFechaAndCategoriaIsNotNull(usuario, fecha);
        List<CategoriaMontoDTO> categoriaMontoDTOList = presupuestoRepository.sumGastosDePresupuestoCategoriaByUsuarioAndFechaAndTipo(usuario.getId(), TipoTransaccion.GASTO.name(), fecha.getYear(), fecha.getMonthValue());
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
            presupuestoRepository.deleteByIdAndUsuario(presupuesto.getId(), usuario);
            return new PresupuestoResponseDTO(null, null, null, null, null, null, null, "El presupuesto fue eliminado porque el monto fue 0");
        }

        if(presupuesto.getCategoria() == null){ // Presupuesto mensual
            return procesarPresupuestoMensual(presupuesto, usuario);
        }

        // Presupuesto de categoría
        return procesarPresupuestoCategoria(presupuesto, usuario);
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
        montoGastado = transaccionRepository.sumMontoTransaccionesByUsuarioAndTipoAndFecha(usuario.getId(), TipoTransaccion.GASTO, presupuesto.getFecha().getYear(), presupuesto.getFecha().getMonthValue());

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
        if (actualizarPresupuestoSiEsNecesario(usuario, presupuesto)){
            mensaje = "El total de los presupuestos por categoría superó el presupuesto mensual, por lo " +
                    "que este último fue actualizado automáticamente.";
        }

        // Obtengo el monto de los gastos totales de dicha categoría
        montoGastado = transaccionRepository.sumMontoTransaccionesByCategoriaAndUsuarioAndTipoAndFecha(usuario.getId(), TipoTransaccion.GASTO, presupuesto.getCategoria(), presupuesto.getFecha().getYear(), presupuesto.getFecha().getMonthValue());

        // El usuario crea un presupuesto de categoría
        return aPresupuestoResumenDTO(presupuestoRepository.save(presupuesto), montoGastado, mensaje);
    }

    public boolean actualizarPresupuestoSiEsNecesario(Usuario usuario, Presupuesto nuevoPresupuesto){
        Presupuesto presupuestoMensual = presupuestoRepository.findPresupuestoMensualByUsuarioAndFechaAndCategoriaIsNull(usuario, nuevoPresupuesto.getFecha());

        if (presupuestoMensual == null) {
            throw new BadRequestException("No existe un presupuesto mensual para esta fecha");
        }

        BigDecimal montoTotalPresupuestado = presupuestoRepository.sumMontoTotalPresupuestadoCategoriasByUsuarioAndFecha(usuario.getId(), nuevoPresupuesto.getFecha().getYear(), nuevoPresupuesto.getFecha().getMonthValue());

        if(nuevoPresupuesto.getId() != null){ // Se busca actualizar un presupuesto
            BigDecimal montoViejoPresupuestado = presupuestoRepository.findMontoPresupuestoCategoriaByUsuarioAndCategoriaAndFecha(usuario.getId(), nuevoPresupuesto.getCategoria(), nuevoPresupuesto.getFecha().getYear(), nuevoPresupuesto.getFecha().getMonthValue());
            if(montoViejoPresupuestado == null){
                montoViejoPresupuestado = BigDecimal.ZERO;
            }
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

    public boolean isExistsPresupuestoMensualPorIDyUsuarioId(Long presupuestoId, Usuario usuario, LocalDate fecha) {
        return presupuestoRepository.existsPresupuestoMensualIdByIdAndUsuarioAndFechaAndCategoriaIsNull(presupuestoId, usuario, fecha);
    }

    public boolean isExistsPresupuestoDeCategoriaPorIDyUsuarioId(Long presupuestoId, Usuario usuario, LocalDate fecha){
        return presupuestoRepository.existsPresupuestoCategoriaByIdAndUsuarioAndFechaAndCategoriaIsNotNull(presupuestoId, usuario, fecha);
    }

    public boolean isExistsPresupuestoMensualPorUsuarioId(Usuario usuario, LocalDate fecha) {
        return presupuestoRepository.existsPresupuestoMensualByUsuarioAndFechaAndCategoriaIsNull(usuario, fecha);
    }

    public boolean isExistsPresupuestoCategoriaPorUsuarioId(Usuario usuario, LocalDate fecha, TipoCategoria categoria){
        return presupuestoRepository.existsPresupuestoDeCategoriaByUsuarioAndFechaAndCategoria(usuario, fecha, categoria);
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
