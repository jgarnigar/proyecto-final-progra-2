package com.proyectofinal.modelo;


public class Accesoriohistorial {

  private long idMovimiento;
  private long idAccesorio;
  private java.sql.Timestamp fecha;
  private long cantidad;
  private String tipoMovimiento;


  public long getIdMovimiento() {
    return idMovimiento;
  }

  public void setIdMovimiento(long idMovimiento) {
    this.idMovimiento = idMovimiento;
  }


  public long getIdAccesorio() {
    return idAccesorio;
  }

  public void setIdAccesorio(long idAccesorio) {
    this.idAccesorio = idAccesorio;
  }


  public java.sql.Timestamp getFecha() {
    return fecha;
  }

  public void setFecha(java.sql.Timestamp fecha) {
    this.fecha = fecha;
  }


  public long getCantidad() {
    return cantidad;
  }

  public void setCantidad(long cantidad) {
    this.cantidad = cantidad;
  }


  public String getTipoMovimiento() {
    return tipoMovimiento;
  }

  public void setTipoMovimiento(String tipoMovimiento) {
    this.tipoMovimiento = tipoMovimiento;
  }

}
