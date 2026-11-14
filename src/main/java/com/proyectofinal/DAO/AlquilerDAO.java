package com.proyectofinal.DAO;

import com.proyectofinal.modelo.AlquilerAccesorios;
import com.proyectofinal.modelo.AlquilerEquipos;
import com.proyectofinal.modelo.Alquileres;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
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
        String sqlEquipo = "INSERT INTO alquiler_equipos (id_alquiler, id_equipo, estado_devolucion) VALUES (?, ?, ?)";
        String sqlAccesorio = "INSERT INTO alquiler_accesorios (id_alquiler, id_accesorio, cantidad, unidades_extraviadas) VALUES (?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement psAlq = con.prepareStatement(sqlAlquiler, Statement.RETURN_GENERATED_KEYS)) {
                psAlq.setInt(1, alquiler.getIdCliente());
                psAlq.setBigDecimal(2, alquiler.getDepositoCobrado());
                psAlq.setString(3, alquiler.getEstado());
                psAlq.setObject(4, alquiler.getFechaEstimada());
                psAlq.setObject(5, alquiler.getFechaReal());

                if (psAlq.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }

                try (ResultSet rs = psAlq.getGeneratedKeys()) {
                    if (rs.next()) {
                        alquiler.setIdAlquiler(rs.getInt(1));
                    } else {
                        con.rollback();
                        return false;
                    }
                }
            }

            if (alquiler.getAlquilerEquiposLista() != null && !alquiler.getAlquilerEquiposLista().isEmpty()) {
                try (PreparedStatement psEq = con.prepareStatement(sqlEquipo)) {
                    for (AlquilerEquipos ae : alquiler.getAlquilerEquiposLista()) {
                        psEq.setInt(1, alquiler.getIdAlquiler());
                        psEq.setInt(2, ae.getIdEquipo());
                        psEq.setString(3, ae.getEstadoDevolucion() != null ? ae.getEstadoDevolucion() : "BUENO");
                        psEq.addBatch();
                    }
                    psEq.executeBatch();
                }
            }

            if (alquiler.getAlquilerAccesoriosLista() != null && !alquiler.getAlquilerAccesoriosLista().isEmpty()) {
                try (PreparedStatement psAcc = con.prepareStatement(sqlAccesorio)) {
                    for (AlquilerAccesorios aa : alquiler.getAlquilerAccesoriosLista()) {
                        psAcc.setInt(1, alquiler.getIdAlquiler());
                        psAcc.setInt(2, aa.getIdAccesorio());
                        psAcc.setInt(3, aa.getCantidad());
                        psAcc.setInt(4, aa.getUnidadesExtraviadas());
                        psAcc.addBatch();
                    }
                    psAcc.executeBatch();
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Alquileres consultar(int idAlquiler) {
        String sql = "SELECT id_alquiler, id_cliente, deposito_cobrado, estado, fecha_estimada, fecha_real FROM alquileres WHERE id_alquiler = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAlquiler);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Alquileres alquiler = mapearAlquiler(rs);
                    alquiler.setAlquilerEquiposLista(obtenerEquiposPorAlquiler(con, idAlquiler));
                    alquiler.setAlquilerAccesoriosLista(obtenerAccesoriosPorAlquiler(con, idAlquiler));
                    return alquiler;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Alquileres> obtenerTodos() {
        List<Alquileres> lista = new ArrayList<>();
        String sql = "SELECT id_alquiler, id_cliente, deposito_cobrado, estado, fecha_estimada, fecha_real FROM alquileres ORDER BY id_alquiler DESC";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Alquileres alquiler = mapearAlquiler(rs);
                alquiler.setAlquilerEquiposLista(obtenerEquiposPorAlquiler(con, alquiler.getIdAlquiler()));
                alquiler.setAlquilerAccesoriosLista(obtenerAccesoriosPorAlquiler(con, alquiler.getIdAlquiler()));
                lista.add(alquiler);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean modificar(Alquileres alquiler) {
        String sqlAlquiler = "UPDATE alquileres SET id_cliente = ?, deposito_cobrado = ?, estado = ?, fecha_estimada = ?, fecha_real = ? WHERE id_alquiler = ?";
        String sqlEquipo = "UPDATE alquiler_equipos SET estado_devolucion = ? WHERE id_alquiler = ? AND id_equipo = ?";
        String sqlAccesorio = "UPDATE alquiler_accesorios SET unidades_extraviadas = ? WHERE id_alquiler = ? AND id_accesorio = ?";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement psAlq = con.prepareStatement(sqlAlquiler)) {
                psAlq.setInt(1, alquiler.getIdCliente());
                psAlq.setBigDecimal(2, alquiler.getDepositoCobrado());
                psAlq.setString(3, alquiler.getEstado());
                psAlq.setObject(4, alquiler.getFechaEstimada());
                psAlq.setObject(5, alquiler.getFechaReal());
                psAlq.setInt(6, alquiler.getIdAlquiler());

                if (psAlq.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            if (alquiler.getAlquilerEquiposLista() != null && !alquiler.getAlquilerEquiposLista().isEmpty()) {
                try (PreparedStatement psEq = con.prepareStatement(sqlEquipo)) {
                    for (AlquilerEquipos ae : alquiler.getAlquilerEquiposLista()) {
                        psEq.setString(1, ae.getEstadoDevolucion() != null ? ae.getEstadoDevolucion() : "BUENO");
                        psEq.setInt(2, alquiler.getIdAlquiler());
                        psEq.setInt(3, ae.getIdEquipo());
                        psEq.addBatch();
                    }
                    psEq.executeBatch();
                }
            }

            if (alquiler.getAlquilerAccesoriosLista() != null && !alquiler.getAlquilerAccesoriosLista().isEmpty()) {
                try (PreparedStatement psAcc = con.prepareStatement(sqlAccesorio)) {
                    for (AlquilerAccesorios aa : alquiler.getAlquilerAccesoriosLista()) {
                        psAcc.setInt(1, aa.getUnidadesExtraviadas());
                        psAcc.setInt(2, alquiler.getIdAlquiler());
                        psAcc.setInt(3, aa.getIdAccesorio());
                        psAcc.addBatch();
                    }
                    psAcc.executeBatch();
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Alquileres mapearAlquiler(ResultSet rs) throws SQLException {
        Alquileres alquiler = new Alquileres();
        alquiler.setIdAlquiler(rs.getInt("id_alquiler"));
        alquiler.setIdCliente(rs.getInt("id_cliente"));
        alquiler.setDepositoCobrado(rs.getBigDecimal("deposito_cobrado"));
        alquiler.setEstado(rs.getString("estado"));
        alquiler.setFechaEstimada(rs.getObject("fecha_estimada", LocalDateTime.class));
        alquiler.setFechaReal(rs.getObject("fecha_real", LocalDateTime.class));
        return alquiler;
    }

    private List<AlquilerEquipos> obtenerEquiposPorAlquiler(Connection con, int idAlquiler) throws SQLException {
        List<AlquilerEquipos> lista = new ArrayList<>();
        String sql = "SELECT id_alquiler, id_equipo, estado_devolucion FROM alquiler_equipos WHERE id_alquiler = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAlquiler);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AlquilerEquipos ae = new AlquilerEquipos();
                    ae.setIdAlquiler(rs.getInt("id_alquiler"));
                    ae.setIdEquipo(rs.getInt("id_equipo"));
                    ae.setEstadoDevolucion(rs.getString("estado_devolucion"));
                    lista.add(ae);
                }
            }
        }
        return lista;
    }

    private List<AlquilerAccesorios> obtenerAccesoriosPorAlquiler(Connection con, int idAlquiler) throws SQLException {
        List<AlquilerAccesorios> lista = new ArrayList<>();
        String sql = "SELECT id_alquiler, id_accesorio, cantidad, unidades_extraviadas FROM alquiler_accesorios WHERE id_alquiler = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAlquiler);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AlquilerAccesorios aa = new AlquilerAccesorios();
                    aa.setIdAlquiler(rs.getInt("id_alquiler"));
                    aa.setIdAccesorio(rs.getInt("id_accesorio"));
                    aa.setCantidad(rs.getInt("cantidad"));
                    aa.setUnidadesExtraviadas(rs.getInt("unidades_extraviadas"));
                    lista.add(aa);
                }
            }
        }
        return lista;
    }
}