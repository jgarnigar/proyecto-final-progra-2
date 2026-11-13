package com.proyectofinal.modelo;


import java.math.BigDecimal;

public class Equipos {

  private int idEquipo;
  private String tipo;
  private String modelo;
  private BigDecimal tarifaHora;
  private BigDecimal precioReposicion;
  private int bateriaPorcentaje;
  private String estado;


  public int getIdEquipo() {
    return idEquipo;
  }

  public void setIdEquipo(int idEquipo) {
    this.idEquipo = idEquipo;
  }


  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }


  public String getModelo() {
    return modelo;
  }

  public void setModelo(String modelo) {
    this.modelo = modelo;
  }


  public BigDecimal getTarifaHora() {
    return tarifaHora;
  }

  public void setTarifaHora(BigDecimal tarifaHora) {
    this.tarifaHora = tarifaHora;
  }


  public BigDecimal getPrecioReposicion() {
    return precioReposicion;
  }

  public void setPrecioReposicion(BigDecimal precioReposicion) {
    this.precioReposicion = precioReposicion;
  }


  public int getBateriaPorcentaje() {
    return bateriaPorcentaje;
  }

  public void setBateriaPorcentaje(int bateriaPorcentaje) {
    this.bateriaPorcentaje = bateriaPorcentaje;
  }


  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

}
