package com.proyectofinal.modelo;

public class AlquilerEquipos {

  private int idAlquiler;
  private int idEquipo;
  private String estadoDevolucion;

  public int getIdAlquiler() {
    return idAlquiler;
  }

  public void setIdAlquiler(int idAlquiler) {
    this.idAlquiler = idAlquiler;
  }

  public int getIdEquipo() {
    return idEquipo;
  }

  public void setIdEquipo(int idEquipo) {
    this.idEquipo = idEquipo;
  }

  public String getEstadoDevolucion() {
    return estadoDevolucion;
  }

  public void setEstadoDevolucion(String estadoDevolucion) {
    this.estadoDevolucion = estadoDevolucion;
  }
}