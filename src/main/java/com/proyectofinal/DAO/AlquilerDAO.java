package com.proyectofinal.DAO;

import com.proyectofinal.modelo.Alquileres;
import com.proyectofinal.modelo.AlquilerAccesorios;
import com.proyectofinal.modelo.AlquilerEquipos;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class AlquilerDAO {

    private final DataSource dataSource;

    public AlquilerDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean registrarAlquiler(Alquileres alquiler) {
        String sqlAlquiler = "INSERT INTO alquileres (id_cliente, deposito_cobrado, estado, fecha_estimada, fecha_real) VALUES (?, ?, ?, ?, ?)";
        String sqlEquipos = "INSERT INTO alquiler_equipos (id_alquiler, id_equipo, estado_devolucion) VALUES (?, ?, ?)";
        String sqlAccesorios = "INSERT INTO alquiler_accesorios (id_alquiler, id_accesorio, cantidad, unidades_extraviadas) VALUES (?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement psAlquiler = con.prepareStatement(sqlAlquiler, Statement.RETURN_GENERATED_KEYS)) {
                psAlquiler.setInt(1, alquiler.getIdCliente());
                psAlquiler.setBigDecimal(2, alquiler.getDepositoCobrado());
                psAlquiler.setString(3, alquiler.getEstado());

                // Usando setObject para LocalDateTime
                psAlquiler.setObject(4, alquiler.getFechaEstimada());

                if (alquiler.getFechaReal() != null) {
                    psAlquiler.setObject(5, alquiler.getFechaReal());
                } else {
                    psAlquiler.setNull(5, Types.TIMESTAMP);
                }

                if (psAlquiler.executeUpdate() == 0) {
                    throw new SQLException("Fallo al registrar el alquiler, no se insertaron filas.");
                }

                try (ResultSet generatedKeys = psAlquiler.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idAlquilerGenerado = generatedKeys.getInt(1);
                        alquiler.setIdAlquiler(idAlquilerGenerado);

                        // Ajustado para usar getAlquilerEquiposLista()
                        if (alquiler.getAlquilerEquiposLista() != null && !alquiler.getAlquilerEquiposLista().isEmpty()) {
                            try (PreparedStatement psEquipos = con.prepareStatement(sqlEquipos)) {
                                for (AlquilerEquipos equipo : alquiler.getAlquilerEquiposLista()) {
                                    psEquipos.setInt(1, idAlquilerGenerado);
                                    psEquipos.setInt(2, equipo.getIdEquipo());
                                    psEquipos.setString(3, equipo.getEstadoDevolucion());
                                    psEquipos.addBatch();
                                }
                                psEquipos.executeBatch();
                            }
                        }

                        // Ajustado para usar getAlquilerAccesoriosLista()
                        if (alquiler.getAlquilerAccesoriosLista() != null && !alquiler.getAlquilerAccesoriosLista().isEmpty()) {
                            try (PreparedStatement psAcc = con.prepareStatement(sqlAccesorios)) {
                                for (AlquilerAccesorios accesorio : alquiler.getAlquilerAccesoriosLista()) {
                                    psAcc.setInt(1, idAlquilerGenerado);
                                    psAcc.setInt(2, accesorio.getIdAccesorio());
                                    psAcc.setInt(3, accesorio.getCantidad());
                                    psAcc.setInt(4, accesorio.getUnidadesExtraviadas());
                                    psAcc.addBatch();
                                }
                                psAcc.executeBatch();
                            }
                        }
                    } else {
                        throw new SQLException("Fallo al obtener el ID generado.");
                    }
                }
                con.commit();
                return true;

            } catch (SQLException ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}