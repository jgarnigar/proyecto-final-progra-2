package com.proyectofinal.DAO;

import com.proyectofinal.modelo.Equipos;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EquiposDAO implements CRUD<Equipos>{

    private final DataSource dataSource;
    public EquiposDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public boolean registrar(Equipos objeto) {

        String SQL = "INSERT INTO equipos(tipo, modelo, tarifa_hora, precio_reposicion, bateria_porcentaje, estado) VALUES(?,?,?,?,?,?)";

        try(Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(SQL)){

            pstmt.setString(1, objeto.getTipo());
            pstmt.setString(2, objeto.getModelo());
            pstmt.setBigDecimal(3, objeto.getTarifaHora());
            pstmt.setBigDecimal(4, objeto.getPrecioReposicion());
            pstmt.setInt(5, objeto.getBateriaPorcentaje());

            return pstmt.executeUpdate() > 0;


        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean modificar(Equipos objeto) {

        String SQL = "UPDATE equipos SET tipo = ?, modelo = ?, tarifa_hora = ?, precio_reposicion = ?, bateria_porcentaje = ?, estado = ? WHERE id_equipo = ?";

        try(Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(SQL)){

            pstmt.setString(1, objeto.getTipo());
            pstmt.setString(2, objeto.getModelo());
            pstmt.setBigDecimal(3, objeto.getTarifaHora());
            pstmt.setBigDecimal(4, objeto.getPrecioReposicion());
            pstmt.setInt(5, objeto.getBateriaPorcentaje());
            pstmt.setString(6,objeto.getEstado());

            pstmt.setInt(7, (int)  objeto.getIdEquipo());

            return pstmt.executeUpdate() > 0;

        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public Equipos consultar(int id) {

        String SQL = "SELECT * FROM equipos WHERE id_equipo = ?";

        try(Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(SQL)){

            pstmt.setInt(1, id);

            try(ResultSet rs = pstmt.executeQuery()){

                Equipos equipo = new Equipos();

                equipo.setIdEquipo(rs.getInt("id_equipo"));
                equipo.setTipo(rs.getString("tipo"));
                equipo.setModelo(rs.getString("modelo"));
                equipo.setTarifaHora(rs.getBigDecimal("tarifa_hora"));
                equipo.setPrecioReposicion(rs.getBigDecimal("precio_reposicion"));
                equipo.setBateriaPorcentaje(rs.getInt("bateria_porcentaje"));
                equipo.setEstado(rs.getString("estado"));

                return equipo;

            }


        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public List<Equipos> listar() {

        String SQL = "SELECT * FROM equipos";

        List<Equipos> equipos = new ArrayList<>();

        try(Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(SQL);
        ResultSet rs = pstmt.executeQuery()){

            while(rs.next()){
                Equipos equipo = new Equipos();

                equipo.setIdEquipo(rs.getInt("id_equipo"));
                equipo.setTipo(rs.getString("tipo"));
                equipo.setModelo(rs.getString("modelo"));
                equipo.setTarifaHora(rs.getBigDecimal("tarifa_hora"));
                equipo.setPrecioReposicion(rs.getBigDecimal("precio_reposicion"));
                equipo.setBateriaPorcentaje(rs.getInt("bateria_porcentaje"));
                equipo.setEstado(rs.getString("estado"));

                equipos.add(equipo);

            }

        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }

        return equipos;
    }
}
