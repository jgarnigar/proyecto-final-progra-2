package com.proyectofinal.modelo;


import java.math.BigDecimal;

public class Clientes {

  private int idCliente;
  private String nombre;
  private String email;
  private BigDecimal saldoPendiente;
  private String estado;


  public long getIdCliente() {
    return idCliente;
  }

  public void setIdCliente(int idCliente) {
    this.idCliente = idCliente;
  }


  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }


  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }


  public BigDecimal getSaldoPendiente() {
    return saldoPendiente;
  }

  public void setSaldoPendiente(BigDecimal saldoPendiente) {
    this.saldoPendiente = saldoPendiente;
  }


  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

}
