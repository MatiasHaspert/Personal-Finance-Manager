package com.example.personal_finance_manager.Repositories;


import com.example.personal_finance_manager.DTOs.CategoriaMontoDTO;
import com.example.personal_finance_manager.Models.Presupuesto;
import com.example.personal_finance_manager.Models.TipoCategoria;
import com.example.personal_finance_manager.Models.TipoTransaccion;
import com.example.personal_finance_manager.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    // Presupuesto mensual está representado por la fila donde el campo "categoría" es null
    boolean existsPresupuestoMensualByUsuarioAndFechaAndCategoriaIsNull(Usuario usuario, LocalDate fecha);

    // Verifica presupuesto de categoría
    boolean existsPresupuestoDeCategoriaByUsuarioAndFechaAndCategoria(Usuario usuario, LocalDate fecha, TipoCategoria categoria);

    // Verifica existencia presupuesto mensual de id específico
    boolean existsPresupuestoMensualIdByIdAndUsuarioAndFechaAndCategoriaIsNull(Long presupuestoId, Usuario usuario, LocalDate fecha);

    // Verifica existencia presupuesto categoría de id específico
    boolean existsPresupuestoCategoriaByIdAndUsuarioAndFechaAndCategoriaIsNotNull(Long presupuestoId, Usuario usuario, LocalDate fecha);
    
    // Obtiene presupuesto mensual
    Presupuesto findPresupuestoMensualByUsuarioAndFechaAndCategoriaIsNull(Usuario usuario, LocalDate fecha);

    // Obtiene presupuestos de categorías.
    List <Presupuesto> findPresupuestosCategoriasByUsuarioAndFechaAndCategoriaIsNotNull(Usuario usuario, LocalDate fecha);

    // Consulta SQL nativa, obtiene los gastos por categoría de los presupuestos de categoría
    @Query(
            value = """
                    SELECT
                        p.categoria AS categoria,
                        COALESCE(SUM(t.monto), 0) AS monto
                    FROM Presupuestos p
                    LEFT JOIN Transacciones t ON t.usuario_id = p.usuario_id
                        AND t.categoria = p.categoria
                        AND t.tipo_transaccion = :tipo
                        AND EXTRACT(YEAR FROM p.fecha) = :anio
                        AND EXTRACT(MONTH FROM p.fecha) = :mes
                    WHERE p.usuario_id = :usuarioId
                        AND p.categoria IS NOT NULL
                        AND EXTRACT(YEAR FROM p.fecha) = :anio
                        AND EXTRACT(MONTH FROM p.fecha) = :mes
                    GROUP BY p.categoria
                    """,
            nativeQuery = true
    )
    List<CategoriaMontoDTO> sumGastosDePresupuestoCategoriaByUsuarioAndFechaAndTipo(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") String tipo,
            @Param("anio") int anio,
            @Param("mes") int mes
    );

    //Consulta JPQL, obtiene la suma de los montos presupuestados de categorías
    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM Presupuesto p
        WHERE p.usuario.id = :usuarioId
            AND p.categoria IS NOT NULL
            AND YEAR(p.fecha) = :anio
            AND MONTH(p.fecha) = :mes
    """)
    BigDecimal sumMontoTotalPresupuestadoCategoriasByUsuarioAndFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("anio") int anio,
            @Param("mes") int mes
    );

    //Consulta JPQL, obtiene el monto presupuestado de un presupuesto de categoría específica
    @Query("""
        SELECT COALESCE(p.monto, 0)
        FROM Presupuesto p
        WHERE p.usuario.id = :usuarioId
            AND p.categoria = :categoria
            AND YEAR(p.fecha) = :anio
            AND MONTH(p.fecha) = :mes
    """)
    BigDecimal findMontoPresupuestoCategoriaByUsuarioAndCategoriaAndFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("categoria") TipoCategoria categoria,
            @Param("anio") int anio,
            @Param("mes") int mes
    );

    void deleteByIdAndUsuario(Long id, Usuario usuario);
}
