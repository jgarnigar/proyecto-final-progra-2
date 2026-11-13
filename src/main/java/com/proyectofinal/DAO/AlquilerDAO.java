package com.proyectofinal.DAO;

import com.proyectofinal.modelo.Alquileres;
import com.proyectofinal.modelo.AlquilerAccesorios;
import com.proyectofinal.modelo.AlquilerEquipos;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<Alquileres> listar() {
        List<Alquileres> lista = new ArrayList<>();
        String sql = "SELECT id_alquiler, id_cliente, deposito_cobrado, estado, fecha_estimada, fecha_real FROM alquileres";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearAlquiler(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return lista;
    }

    public Alquileres consultar(int idAlquiler) {
        String sqlAlquiler = "SELECT id_alquiler, id_cliente, deposito_cobrado, estado, fecha_estimada, fecha_real FROM alquileres WHERE id_alquiler = ?";
        String sqlEquipos = "SELECT id_alquiler, id_equipo, estado_devolucion FROM alquiler_equipos WHERE id_alquiler = ?";
        String sqlAccesorios = "SELECT id_alquiler, id_accesorio, cantidad, unidades_extraviadas FROM alquiler_accesorios WHERE id_alquiler = ?";

        Alquileres alquiler = null;

        try (Connection con = dataSource.getConnection()) {


            try (PreparedStatement ps = con.prepareStatement(sqlAlquiler)) {
                ps.setInt(1, idAlquiler);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        alquiler = mapearAlquiler(rs);
                    }
                }
            }

            if (alquiler != null) {

                try (PreparedStatement psEq = con.prepareStatement(sqlEquipos)) {
                    psEq.setInt(1, idAlquiler);
                    try (ResultSet rsEq = psEq.executeQuery()) {
                        while (rsEq.next()) {
                            AlquilerEquipos eq = new AlquilerEquipos();
                            eq.setIdAlquiler(rsEq.getInt("id_alquiler"));
                            eq.setIdEquipo(rsEq.getInt("id_equipo"));
                            eq.setEstadoDevolucion(rsEq.getString("estado_devolucion"));
                            alquiler.agregarAlquilerEquipos(eq);
                        }
                    }
                }

                try (PreparedStatement psAcc = con.prepareStatement(sqlAccesorios)) {
                    psAcc.setInt(1, idAlquiler);
                    try (ResultSet rsAcc = psAcc.executeQuery()) {
                        while (rsAcc.next()) {
                            AlquilerAccesorios acc = new AlquilerAccesorios();
                            acc.setIdAlquiler(rsAcc.getInt("id_alquiler"));
                            acc.setIdAccesorio(rsAcc.getInt("id_accesorio"));
                            acc.setCantidad(rsAcc.getInt("cantidad"));
                            acc.setUnidadesExtraviadas(rsAcc.getInt("unidades_extraviadas"));
                            alquiler.agregarAlquilerAccesorios(acc);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alquiler;
    }

    public boolean modificar(Alquileres alquiler) {
        String sql = "UPDATE alquileres SET deposito_cobrado = ?, estado = ?, fecha_estimada = ?, fecha_real = ? WHERE id_alquiler = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, alquiler.getDepositoCobrado());
            ps.setString(2, alquiler.getEstado());
            ps.setObject(3, alquiler.getFechaEstimada());

            if (alquiler.getFechaReal() != null) {
                ps.setObject(4, alquiler.getFechaReal());
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            ps.setInt(5, alquiler.getIdAlquiler());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Alquileres mapearAlquiler(ResultSet rs) throws SQLException {
        Alquileres a = new Alquileres();
        a.setIdAlquiler(rs.getInt("id_alquiler"));
        a.setIdCliente(rs.getInt("id_cliente"));
        a.setDepositoCobrado(rs.getBigDecimal("deposito_cobrado"));
        a.setEstado(rs.getString("estado"));

        Timestamp tsEstimada = rs.getTimestamp("fecha_estimada");
        if (tsEstimada != null) a.setFechaEstimada(tsEstimada.toLocalDateTime());

        Timestamp tsReal = rs.getTimestamp("fecha_real");
        if (tsReal != null) a.setFechaReal(tsReal.toLocalDateTime());

        return a;
    }

}