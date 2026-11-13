package com.proyectofinal.modelo;

import java.time.LocalDateTime;

public class Accesoriohistorial {

  private int idMovimiento;
  private int idAccesorio;
  private LocalDateTime fecha;
  private int cantidad;
  private String tipoMovimiento;

  public int getIdMovimiento() {
    return idMovimiento;
  }

  public void setIdMovimiento(int idMovimiento) {
    this.idMovimiento = idMovimiento;
  }

  public int getIdAccesorio() {
    return idAccesorio;
  }

  public void setIdAccesorio(int idAccesorio) {
    this.idAccesorio = idAccesorio;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  public int getCantidad() {
    return cantidad;
  }

  public void setCantidad(int cantidad) {
    this.cantidad = cantidad;
  }

  public String getTipoMovimiento() {
    return tipoMovimiento;
  }

  public void setTipoMovimiento(String tipoMovimiento) {
    this.tipoMovimiento = tipoMovimiento;
  }
}