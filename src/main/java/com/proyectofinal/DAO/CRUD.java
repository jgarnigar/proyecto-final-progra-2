package com.proyectofinal.DAO;

import java.util.List;

public interface CRUD<T> {

    boolean registrar(T objeto);

    boolean modificar(T objeto);

    T consultar(int id);

    List<T> listar();

}
