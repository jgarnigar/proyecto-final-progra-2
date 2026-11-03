package com.proyectofinal.DAO;

import com.proyectofinal.modelo.Clientes;

import org.hibernate.annotations.processing.SQL;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ClientesDAO implements CRUD<Clientes>{

    private final DataSource dataSource;

    public ClientesDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public boolean registrar(Clientes objeto) {

        String SQL = "INSERT INTO clientes(nombre, email, saldo_pendiente, estado) VALUES(?,?,?,?)";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(SQL)){

            pstmt.setString(1, objeto.getNombre());
            pstmt.setString(2, objeto.getEmail());
            pstmt.setBigDecimal(3, objeto.getSaldoPendiente());
            pstmt.setString(4, objeto.getEstado());

            return pstmt.executeUpdate() > 0;


        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean modificar(Clientes objeto) {

        String SQL = "UPDATE clientes SET nombre = ?, email = ?, saldo_pendiente = ?, estado = ? WHERE id_cliente = ?";

        try(Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(SQL)){

            pstmt.setString(1, objeto.getNombre());
            pstmt.setString(2, objeto.getEmail());
            pstmt.setBigDecimal(3, objeto.getSaldoPendiente());
            pstmt.setString(4, objeto.getEstado());

            pstmt.setInt(5, (int) objeto.getIdCliente());

            return pstmt.executeUpdate() > 0;

        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public Clientes consultar(int id) {

        String SQL = "SELECT * FROM clientes WHERE id_cliente = ?";

        try(Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(SQL)){

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()){

                if(rs.next()){

                    Clientes cliente = new  Clientes();
                    cliente.setIdCliente(rs.getInt("id_cliente"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente"));
                    cliente.setEstado(rs.getString("estado"));
                    return cliente;

                }

            }


        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }

        return null;
    }

    @Override
    public List<Clientes> listar() {

        String  SQL = "SELECT * FROM clientes";

        List<Clientes> clientes = new ArrayList<>();

        try(Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(SQL);
        ResultSet rs = pstmt.executeQuery()){

            while(rs.next()){

                Clientes cliente = new Clientes();

                cliente.setIdCliente(rs.getInt("id_cliente"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setEmail(rs.getString("email"));
                cliente.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente"));
                cliente.setEstado(rs.getString("estado"));
                clientes.add(cliente);;

            }


        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }

        return clientes;
    }
}
