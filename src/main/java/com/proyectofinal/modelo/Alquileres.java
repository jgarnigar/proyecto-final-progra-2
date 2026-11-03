package com.proyectofinal.modelo;


import java.util.List;

public class Alquileres {

  private long idAlquiler;
  private long idCliente;
  private double depositoCobrado;
  private String estado;
  private java.sql.Timestamp fechaEstimada;
  private java.sql.Timestamp fechaReal;

  private List<AlquilerAccesorios> alquilerAccesoriosLista;
  private List<AlquilerEquipos> alquilerEquiposLista;

  public long getIdAlquiler() {
    return idAlquiler;
  }

  public void setIdAlquiler(long idAlquiler) {
    this.idAlquiler = idAlquiler;
  }


  public long getIdCliente() {
    return idCliente;
  }

  public void setIdCliente(long idCliente) {
    this.idCliente = idCliente;
  }


  public double getDepositoCobrado() {
    return depositoCobrado;
  }

  public void setDepositoCobrado(double depositoCobrado) {
    this.depositoCobrado = depositoCobrado;
  }


  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }


  public java.sql.Timestamp getFechaEstimada() {
    return fechaEstimada;
  }

  public void setFechaEstimada(java.sql.Timestamp fechaEstimada) {
    this.fechaEstimada = fechaEstimada;
  }


  public java.sql.Timestamp getFechaReal() {
    return fechaReal;
  }

  public void setFechaReal(java.sql.Timestamp fechaReal) {
    this.fechaReal = fechaReal;
  }

  public void agregarAlquilerAccesorios(AlquilerAccesorios alquilerAccesorios) {
    this.alquilerAccesoriosLista.add(alquilerAccesorios);
  }

  public void agregarAlquierEquipos(AlquilerEquipos alquierEquipos) {
    this.alquilerEquiposLista.add(alquierEquipos);
  }

  public boolean removerAlquierAccesorios(int idAlquiler){

      return this.alquilerAccesoriosLista.removeIf(objeto -> objeto.getIdAlquiler() == idAlquiler);
  }

  public boolean removerAlquierEquipos(int idAlquipos){

      return this.alquilerEquiposLista.removeIf(objeto -> objeto.getIdAlquiler() == idAlquipos);
  }

}
