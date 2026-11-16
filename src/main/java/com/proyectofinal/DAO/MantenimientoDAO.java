package com.proyectofinal.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.proyectofinal.modelo.Mantenimientos;

@Repository
public class MantenimientoDAO {

    private final DataSource dataSource;

    public MantenimientoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Registrar un nuevo mantenimiento
    public boolean registrarMantenimiento(Mantenimientos mantenimiento) {
        String sql = "INSERT INTO mantenimientos (id_equipo, fecha, motivo, costo) VALUES (?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, mantenimiento.getIdEquipo());
            ps.setObject(2, mantenimiento.getFecha());
            ps.setString(3, mantenimiento.getMotivo());
            ps.setBigDecimal(4, mantenimiento.getCosto());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        mantenimiento.setIdMantenimiento(rs.getInt(1));
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

    // Consultar el historial de un equipo en particular
    public List<Mantenimientos> obtenerMantenimientosPorEquipo(int idEquipo) {
        List<Mantenimientos> lista = new ArrayList<>();
        String sql = "SELECT id_mantenimiento, id_equipo, fecha, motivo, costo FROM mantenimientos WHERE id_equipo = ? ORDER BY fecha DESC";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEquipo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Mantenimientos m = new Mantenimientos();
                    m.setIdMantenimiento(rs.getInt("id_mantenimiento"));
                    m.setIdEquipo(rs.getInt("id_equipo"));
                    m.setFecha(rs.getObject("fecha", LocalDateTime.class));
                    m.setMotivo(rs.getString("motivo"));
                    m.setCosto(rs.getBigDecimal("costo"));

                    lista.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
    public int contarAlquileresDesdeUltimoMantenimiento(int idEquipo) {
        String sql = "SELECT COUNT(*) FROM alquiler_equipos ae "
                + "JOIN alquileres a ON a.id_alquiler = ae.id_alquiler "
                + "WHERE ae.id_equipo = ? AND a.estado = 'FINALIZADO' "
                + "AND a.fecha_real > COALESCE((SELECT MAX(m.fecha) FROM mantenimientos m WHERE m.id_equipo = ?), '1000-01-01 00:00:00')";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEquipo);
            ps.setInt(2, idEquipo);

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
}
