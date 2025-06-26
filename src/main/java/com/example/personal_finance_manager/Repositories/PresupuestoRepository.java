package com.example.personal_finance_manager.Repositories;


import com.example.personal_finance_manager.DTOs.CategoriaMontoDTO;
import com.example.personal_finance_manager.Models.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {


    // Consulta SQL nativa, presupuesto mensual está representado por la fila donde el campo "categoría" es null
    @Query(
            value = """
                    SELECT
                        COUNT(*) > 0
                    FROM Presupuestos p
                    WHERE p.usuario_id = :usuarioId
                        AND EXTRACT(YEAR FROM p.fecha) = :anio
                        AND EXTRACT(MONTH FROM p.fecha) = :mes
                        AND p.categoria IS NULL
                    """,
            nativeQuery = true
    )
    boolean existsPresupuestoMensualByUsuarioAndFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("anio") int anio,
            @Param("mes") int mes
    );

    // Consulta SQL nativa para presupuesto de categoría específico
    @Query(
            value = """
                    SELECT
                        COUNT(*) > 0
                    FROM Presupuestos p
                    WHERE p.usuario_id = :usuarioId
                        AND EXTRACT(YEAR FROM p.fecha) = :anio
                        AND EXTRACT(MONTH FROM p.fecha) = :mes
                        AND p.categoria = :categoria
                    """,
            nativeQuery = true
    )
    boolean existsPresupuestoDeCategoriaByUsuarioAndFechaAndCategoria(
            @Param("usuarioId") Long usuarioId,
            @Param("anio") int anio,
            @Param("mes") int mes,
            @Param("categoria") String categoria
    );

    // Consulta SQL nativa, verifica existencia con ID para presupuesto mensual
    @Query(
            value = """
                    SELECT
                        COUNT(*) > 0
                    FROM Presupuestos p
                    WHERE p.presupuesto_id = :presupuestoId
                        AND p.usuario_id = :usuarioId
                        AND EXTRACT(YEAR FROM p.fecha) = :anio
                        AND EXTRACT(MONTH FROM p.fecha) = :mes
                        AND p.categoria IS NULL
                    """,
            nativeQuery = true
    )
    boolean existsPresupuestoMensualIdByIdAndUsuarioAndFecha(
            @Param("presupuestoId") Long presupuestoId,
            @Param("usuarioId") Long usuarioId,
            @Param("anio") int anio,
            @Param("mes") int mes
    );

    // Consulta SQL nativa, verifica existencia con ID para presupuesto de categoría específica
    @Query(
            value = """
                    SELECT
                        COUNT(*) > 0
                    FROM Presupuestos p
                    WHERE p.presupuesto_id = :presupuestoId
                        AND p.usuario_id = :usuarioId
                        AND EXTRACT(YEAR FROM p.fecha) = :anio
                        AND EXTRACT(MONTH FROM p.fecha) = :mes
                    """,
            nativeQuery = true
    )
    boolean existsPresupuestoDeCategoriaIdByIdAndUsuarioAndFecha(
            @Param("presupuestoId") Long presupuestoId,
            @Param("usuarioId") Long usuarioId,
            @Param("anio") int anio,
            @Param("mes") int mes
    );
    
    // Consulta SQL nativa, obtiene presupuesto mensual
    @Query(
            value = """
                    SELECT
                        *
                    FROM Presupuestos p
                    WHERE p.usuario_id = :usuarioId
                        AND EXTRACT(YEAR FROM p.fecha) = :anio
                        AND EXTRACT(MONTH FROM p.fecha) = :mes
                        AND p.categoria IS NULL
                    """,
            nativeQuery = true
    )
    Presupuesto findPresupuestoMensualByUsuarioAndFechaAndCategoriaIsNull(
            @Param("usuarioId") Long usuarioId,
            @Param("anio") int anio,
            @Param("mes") int mes
    );

    // Consulta SQL nativa, obtiene presupuestos de categorías.
    @Query(
            value = """
                    SELECT
                        *
                    FROM Presupuestos p
                    WHERE p.usuario_id = :usuarioId
                        AND EXTRACT(YEAR FROM p.fecha) = :anio
                        AND EXTRACT(MONTH FROM p.fecha) = :mes
                        AND p.categoria IS NOT NULL
                    """,
            nativeQuery = true
    )
    List <Presupuesto> findPresupuestosDeCategoriasByUsuarioAndFechaAndCategoriaisNotNull(
            @Param("usuarioId") Long usuarioId,
            @Param("anio") int anio,
            @Param("mes") int mes
    );

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

    //Consulta SQL nativo, obtiene la suma de los montos presupuestados de categorías
    @Query(
            value = """
                    SELECT
                        COALESCE(SUM(p.monto), 0)
                    FROM Presupuestos p
                    WHERE p.usuario_id = :usuarioId
                        AND p.categoria IS NOT NULL
                        AND EXTRACT(YEAR FROM p.fecha) = :anio
                        AND EXTRACT(MONTH FROM p.fecha) = :mes
                    """,
            nativeQuery = true
    )
    BigDecimal sumMontoTotalPresupuestadoCategoriasByUsuarioAndFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("anio") int anio,
            @Param("mes") int mes
    );

    //Consulta SQL nativo, obtiene el monto presupuestado de un presupuesto de categoría específica
    @Query(
            value = """
                    SELECT
                        COALESCE(p.monto, 0)
                    FROM Presupuestos p
                    WHERE p.usuario_id = :usuarioId
                        AND p.categoria = :categoria
                        AND EXTRACT(YEAR FROM p.fecha) = :anio
                        AND EXTRACT(MONTH FROM p.fecha) = :mes
                    """,
            nativeQuery = true
    )
    BigDecimal findMontoPresupuestoCategoriaByUsuarioAndCategoriaAndFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("categoria") String categoria,
            @Param("anio") int anio,
            @Param("mes") int mes
    );
}
