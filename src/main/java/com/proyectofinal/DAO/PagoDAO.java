package com.proyectofinal.DAO;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Repository
public class PagoDAO {

    private final DataSource dataSource;

    public PagoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean registrarPago(int idAlquiler, LocalDateTime fechaPago, String metodoPago, BigDecimal monto, String referencia) {
        String sql = "INSERT INTO pagos (id_alquiler, fecha_pago, metodo_pago, monto, numero_referencia) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAlquiler);
            ps.setObject(2, fechaPago);
            ps.setString(3, metodoPago);
            ps.setBigDecimal(4, monto);
            ps.setString(5, referencia);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}