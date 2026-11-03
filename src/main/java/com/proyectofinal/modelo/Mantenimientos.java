package com.proyectofinal.modelo;


public class Mantenimientos {

  private long idMantenimiento;
  private long idEquipo;
  private java.sql.Timestamp fecha;
  private String motivo;
  private double costo;


  public long getIdMantenimiento() {
    return idMantenimiento;
  }

  public void setIdMantenimiento(long idMantenimiento) {
    this.idMantenimiento = idMantenimiento;
  }


  public long getIdEquipo() {
    return idEquipo;
  }

  public void setIdEquipo(long idEquipo) {
    this.idEquipo = idEquipo;
  }


  public java.sql.Timestamp getFecha() {
    return fecha;
  }

  public void setFecha(java.sql.Timestamp fecha) {
    this.fecha = fecha;
  }


  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }


  public double getCosto() {
    return costo;
  }

  public void setCosto(double costo) {
    this.costo = costo;
  }

}
