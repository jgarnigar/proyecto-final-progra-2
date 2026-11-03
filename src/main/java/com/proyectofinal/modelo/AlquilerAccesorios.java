package com.proyectofinal.modelo;


public class AlquilerAccesorios {

  private long idAlquiler;
  private long idAccesorio;
  private long cantidad;
  private long unidadesExtraviadas;


  public long getIdAlquiler() {
    return idAlquiler;
  }

  public void setIdAlquiler(long idAlquiler) {
    this.idAlquiler = idAlquiler;
  }


  public long getIdAccesorio() {
    return idAccesorio;
  }

  public void setIdAccesorio(long idAccesorio) {
    this.idAccesorio = idAccesorio;
  }


  public long getCantidad() {
    return cantidad;
  }

  public void setCantidad(long cantidad) {
    this.cantidad = cantidad;
  }


  public long getUnidadesExtraviadas() {
    return unidadesExtraviadas;
  }

  public void setUnidadesExtraviadas(long unidadesExtraviadas) {
    this.unidadesExtraviadas = unidadesExtraviadas;
  }

}
