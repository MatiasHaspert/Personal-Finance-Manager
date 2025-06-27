package com.example.personal_finance_manager.Repositories;


import com.example.personal_finance_manager.DTOs.CategoriaMontoDTO;
import com.example.personal_finance_manager.Models.TipoCategoria;
import com.example.personal_finance_manager.Models.TipoTransaccion;
import com.example.personal_finance_manager.Models.Transaccion;
import com.example.personal_finance_manager.Models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    void deleteTransaccionByIdAndUsuario(Long id, Usuario usuario);

    // Obtiene transacciones de un determinado periodo(año y mes) ordenadas de manera descendente
    List<Transaccion> findTransaccionesByUsuarioAndTipoTransaccionAndFecha(Usuario usuario, TipoTransaccion tipo, LocalDate fecha);

    // Consulta JPQL, obtiene la suma de los montos de transacciones de un determinado tipo (INGRESO O GASTO)
    @Query("""
        SELECT COALESCE(SUM(t.monto), 0)
        FROM Transaccion t
        WHERE t.usuario.id = :usuarioId
            AND t.tipoTransaccion = :tipo
            AND YEAR(t.fecha) = :anio
            AND MONTH(t.fecha) = :mes
    """)
    BigDecimal sumMontoTransaccionesByUsuarioAndTipoAndFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoTransaccion tipo,
            @Param("anio") int anio,
            @Param("mes") int mes
            );


    // Consulta SQL nativa, obtiene las categorías y monto total asociado por tipo (INGRESO o GASTO)
    @Query(
            value = """
                    SELECT
                        t.categoria AS categoria, sum(t.monto) AS monto
                    FROM Transacciones t
                    WHERE t.usuario_id = :usuarioId
                        AND t.tipo_transaccion = :tipo
                        AND EXTRACT(YEAR FROM t.fecha) = :anio
                        AND EXTRACT(MONTH FROM t.fecha) = :mes
                    GROUP BY t.categoria
                    """,
            nativeQuery = true
    )
    List<CategoriaMontoDTO> getCategoriaMontoByUsuarioAndTipoAndFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") String tipo,
            @Param("anio") int anio,
            @Param("mes") int mes
    );

    // Consulta JPQL, obtiene transacciones de un determinado periodo(año y mes) con paginacíón y orden descendente por fecha.
    @Query("""
        SELECT t FROM Transaccion t
        WHERE t.usuario.id = :usuarioId
            AND YEAR(t.fecha) = :anio
            AND MONTH(t.fecha) = :mes
        ORDER BY t.fecha DESC
    """)
    Page<Transaccion> findTransaccionesPaginadasByUsuarioAndFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("anio") int anio,
            @Param("mes") int mes,
            Pageable pageable
    );

    // Consulta JPQL, obtiene la suma de los montos de transacciones de una categoría específica y tipo (INGRESO o GASTO)
    @Query("""
        SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t
        WHERE t.usuario.id = :usuarioId
            AND t.tipoTransaccion = :tipo
            AND YEAR(t.fecha) = :anio
            AND MONTH(t.fecha) = :mes
            AND t.categoria = :categoria
    """)
    BigDecimal sumMontoTransaccionesByCategoriaAndUsuarioAndTipoAndFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoTransaccion tipo,
            @Param("categoria") TipoCategoria categoria,
            @Param("anio") int anio,
            @Param("mes") int mes
    );
}
