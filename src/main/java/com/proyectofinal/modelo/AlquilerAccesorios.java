package com.proyectofinal.modelo;

public class AlquilerAccesorios {

  private int idAlquiler;
  private int idAccesorio;
  private int cantidad;
  private int unidadesExtraviadas;

  public int getIdAlquiler() {
    return idAlquiler;
  }

  public void setIdAlquiler(int idAlquiler) {
    this.idAlquiler = idAlquiler;
  }

  public int getIdAccesorio() {
    return idAccesorio;
  }

  public void setIdAccesorio(int idAccesorio) {
    this.idAccesorio = idAccesorio;
  }

  public int getCantidad() {
    return cantidad;
  }

  public void setCantidad(int cantidad) {
    this.cantidad = cantidad;
  }

  public int getUnidadesExtraviadas() {
    return unidadesExtraviadas;
  }

  public void setUnidadesExtraviadas(int unidadesExtraviadas) {
    this.unidadesExtraviadas = unidadesExtraviadas;
  }
}