package com.proyectofinal.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Mantenimientos {

  private int idMantenimiento;
  private int idEquipo;
  private LocalDateTime fecha;
  private String motivo;
  private BigDecimal costo;

  public int getIdMantenimiento() {
    return idMantenimiento;
  }

  public void setIdMantenimiento(int idMantenimiento) {
    this.idMantenimiento = idMantenimiento;
  }

  public int getIdEquipo() {
    return idEquipo;
  }

  public void setIdEquipo(int idEquipo) {
    this.idEquipo = idEquipo;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }

  public BigDecimal getCosto() {
    return costo;
  }

  public void setCosto(BigDecimal costo) {
    this.costo = costo;
  }
}