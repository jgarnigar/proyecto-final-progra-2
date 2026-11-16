package com.proyectofinal.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.proyectofinal.modelo.Accesoriohistorial;

@Repository
public class AccesorioHistorialDAO {

    private final DataSource dataSource;

    public AccesorioHistorialDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //  Registrar un nuevo movimiento de inventario (Entrada/Salida)
    public boolean registrarMovimiento(Accesoriohistorial historial) {
        String sql = "INSERT INTO accesoriohistorial (id_accesorio, fecha, cantidad, tipo_movimiento) VALUES (?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, historial.getIdAccesorio());
            ps.setObject(2, historial.getFecha());
            ps.setInt(3, historial.getCantidad());
            ps.setString(4, historial.getTipoMovimiento());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        historial.setIdMovimiento(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Accesoriohistorial> obtenerHistorialPorAccesorio(int idAccesorio) {
        List<Accesoriohistorial> lista = new ArrayList<>();
        String sql = "SELECT id_movimiento, id_accesorio, fecha, cantidad, tipo_movimiento FROM accesoriohistorial WHERE id_accesorio = ? ORDER BY fecha DESC";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAccesorio);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Accesoriohistorial h = new Accesoriohistorial();
                    h.setIdMovimiento(rs.getInt("id_movimiento"));
                    h.setIdAccesorio(rs.getInt("id_accesorio"));

                    h.setFecha(rs.getObject("fecha", LocalDateTime.class));
                    h.setCantidad(rs.getInt("cantidad"));
                    h.setTipoMovimiento(rs.getString("tipo_movimiento"));

                    lista.add(h);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;

    }
    public int unidadesEnMantenimiento(int idAccesorio) {
        String sql = "SELECT COALESCE(SUM(CASE WHEN tipo_movimiento = 'SALIDA_POR_MANTENIMIENTO' THEN cantidad "
                + "WHEN tipo_movimiento = 'ENTRADA_POR_MANTENIMIENTO' THEN -cantidad ELSE 0 END), 0) "
                + "FROM accesoriohistorial WHERE id_accesorio = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAccesorio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<Map<String, Object>> listarEnMantenimiento() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT a.id_accesorio, a.nombre, "
                + "SUM(CASE WHEN h.tipo_movimiento = 'SALIDA_POR_MANTENIMIENTO' THEN h.cantidad "
                + "WHEN h.tipo_movimiento = 'ENTRADA_POR_MANTENIMIENTO' THEN -h.cantidad ELSE 0 END) AS en_mantenimiento "
                + "FROM accesoriohistorial h JOIN accesorios a ON a.id_accesorio = h.id_accesorio "
                + "GROUP BY a.id_accesorio, a.nombre HAVING en_mantenimiento > 0 ORDER BY a.nombre";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("idAccesorio", rs.getInt("id_accesorio"));
                fila.put("nombre", rs.getString("nombre"));
                fila.put("enMantenimiento", rs.getInt("en_mantenimiento"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

}