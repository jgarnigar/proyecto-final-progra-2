package com.proyectofinal.DAO;

import com.proyectofinal.modelo.Accesorios;
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
public class AccesoriosDAO implements CRUD<Accesorios>{

    private final DataSource datasource;

    public  AccesoriosDAO(DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public boolean registrar(Accesorios objeto) {

        String SQL = "INSERT INTO accesorios(nombre, tarifa_fija, precio_reposicion, existencia_actual) VALUES(?,?,?,?)";

        try(Connection connection = datasource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(SQL)){

            pstmt.setString(1, objeto.getNombre());
            pstmt.setBigDecimal(2, objeto.getTarifaFija());
            pstmt.setBigDecimal(3, objeto.getPrecioReposicion());
            pstmt.setInt(4, objeto.getExistenciaActual());

            return pstmt.executeUpdate() > 0;

        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean modificar(Accesorios objeto) {
        String SQL = "UPDATE accesorios SET nombre = ?, tarifa_fija = ?, precio_reposicion = ?, existencia_actual = ? WHERE id_accesorio = ?";

        try(Connection connection = datasource.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(SQL)){

            pstmt.setString(1, objeto.getNombre());
            pstmt.setBigDecimal(2, objeto.getTarifaFija());
            pstmt.setBigDecimal(3, objeto.getPrecioReposicion());
            pstmt.setInt(4, objeto.getExistenciaActual());

            pstmt.setInt(5, (int)  objeto.getIdAccesorio());

            return pstmt.executeUpdate() > 0;

        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Accesorios consultar(int id) {
        String SQL = "SELECT * FROM accesorios WHERE id_accesorio = ?";

        try(Connection connection = datasource.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(SQL)){

            pstmt.setInt(1, id);

            try(ResultSet rs = pstmt.executeQuery()){

                Accesorios accesorio = new Accesorios();

                accesorio.setIdAccesorio(rs.getInt("id_accesorio"));
                accesorio.setNombre(rs.getString("nombre"));
                accesorio.setTarifaFija(rs.getBigDecimal("tarifa_fija"));
                accesorio.setPrecioReposicion(rs.getBigDecimal("precio_reposicion"));
                accesorio.setExistenciaActual(rs.getInt("existencia_actual"));


                return accesorio;

            }


        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Accesorios> listar() {

        String SQL = "SELECT * FROM accesorios";

        List<Accesorios> accesorios = new ArrayList<>();

        try(Connection connection = datasource.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            ResultSet rs = pstmt.executeQuery()){

            while(rs.next()){

                Accesorios accesorio = new Accesorios();

                accesorio.setIdAccesorio(rs.getInt("id_accesorio"));
                accesorio.setNombre(rs.getString("nombre"));
                accesorio.setTarifaFija(rs.getBigDecimal("tarifa_fija"));
                accesorio.setPrecioReposicion(rs.getBigDecimal("precio_reposicion"));
                accesorio.setExistenciaActual(rs.getInt("existencia_actual"));

                accesorios.add(accesorio);

            }

        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }

        return accesorios;
    }
}
