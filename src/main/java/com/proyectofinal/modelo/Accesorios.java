package com.proyectofinal.modelo;


import java.math.BigDecimal;

public class Accesorios {

  private int idAccesorio;
  private String nombre;
  private BigDecimal tarifaFija;
  private BigDecimal precioReposicion;
  private int existenciaActual;


  public long getIdAccesorio() {
    return idAccesorio;
  }

  public void setIdAccesorio(int idAccesorio) {
    this.idAccesorio = idAccesorio;
  }


  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }


  public BigDecimal getTarifaFija() {
    return tarifaFija;
  }

  public void setTarifaFija(BigDecimal tarifaFija) {
    this.tarifaFija = tarifaFija;
  }


  public BigDecimal getPrecioReposicion() {
    return precioReposicion;
  }

  public void setPrecioReposicion(BigDecimal precioReposicion) {
    this.precioReposicion = precioReposicion;
  }


  public int getExistenciaActual() {
    return existenciaActual;
  }

  public void setExistenciaActual(int existenciaActual) {
    this.existenciaActual = existenciaActual;
  }

}
