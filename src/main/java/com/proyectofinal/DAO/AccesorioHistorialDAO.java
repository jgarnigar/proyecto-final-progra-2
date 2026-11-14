package com.proyectofinal.DAO;

import com.proyectofinal.modelo.Accesoriohistorial;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AccesorioHistorialDAO {

    private final DataSource dataSource;

    public AccesorioHistorialDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //  Registrar un nuevo movimiento de inventario (Entrada/Salida)
    public boolean registrarMovimiento(Accesoriohistorial historial) {
        String sql = "INSERT INTO accesorio_historial (id_accesorio, fecha, cantidad, tipo_movimiento) VALUES (?, ?, ?, ?)";

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
        String sql = "SELECT id_movimiento, id_accesorio, fecha, cantidad, tipo_movimiento FROM accesorio_historial WHERE id_accesorio = ? ORDER BY fecha DESC";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAccesorio);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Accesoriohistorial h = new Accesoriohistorial();
                    h.setIdMovimiento(rs.getInt("id_movimiento"));
                    h.setIdAccesorio(rs.getInt("id_accesorio"));

                    // Usando getObject para recuperar el LocalDateTime
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
}