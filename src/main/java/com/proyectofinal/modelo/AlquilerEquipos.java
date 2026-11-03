package com.proyectofinal.modelo;


public class AlquilerEquipos {

  private long idAlquiler;
  private long idEquipo;
  private String estadoDevolucion;


  public long getIdAlquiler() {
    return idAlquiler;
  }

  public void setIdAlquiler(long idAlquiler) {
    this.idAlquiler = idAlquiler;
  }


  public long getIdEquipo() {
    return idEquipo;
  }

  public void setIdEquipo(long idEquipo) {
    this.idEquipo = idEquipo;
  }


  public String getEstadoDevolucion() {
    return estadoDevolucion;
  }

  public void setEstadoDevolucion(String estadoDevolucion) {
    this.estadoDevolucion = estadoDevolucion;
  }

}
