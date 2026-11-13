package com.proyectofinal.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Alquileres {

  private int idAlquiler;
  private int idCliente;
  private BigDecimal depositoCobrado;
  private String estado;
  private LocalDateTime fechaEstimada;
  private LocalDateTime fechaReal;

  // Inicialización para evitar NullPointerException al agregar elementos
  private List<AlquilerAccesorios> alquilerAccesoriosLista = new ArrayList<>();
  private List<AlquilerEquipos> alquilerEquiposLista = new ArrayList<>();

  public int getIdAlquiler() {
    return idAlquiler;
  }

  public void setIdAlquiler(int idAlquiler) {
    this.idAlquiler = idAlquiler;
  }

  public int getIdCliente() {
    return idCliente;
  }

  public void setIdCliente(int idCliente) {
    this.idCliente = idCliente;
  }

  public BigDecimal getDepositoCobrado() {
    return depositoCobrado;
  }

  public void setDepositoCobrado(BigDecimal depositoCobrado) {
    this.depositoCobrado = depositoCobrado;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaEstimada() {
    return fechaEstimada;
  }

  public void setFechaEstimada(LocalDateTime fechaEstimada) {
    this.fechaEstimada = fechaEstimada;
  }

  public LocalDateTime getFechaReal() {
    return fechaReal;
  }

  public void setFechaReal(LocalDateTime fechaReal) {
    this.fechaReal = fechaReal;
  }

  public List<AlquilerAccesorios> getAlquilerAccesoriosLista() {
    return alquilerAccesoriosLista;
  }

  public void setAlquilerAccesoriosLista(List<AlquilerAccesorios> alquilerAccesoriosLista) {
    this.alquilerAccesoriosLista = alquilerAccesoriosLista;
  }

  public List<AlquilerEquipos> getAlquilerEquiposLista() {
    return alquilerEquiposLista;
  }

  public void setAlquilerEquiposLista(List<AlquilerEquipos> alquilerEquiposLista) {
    this.alquilerEquiposLista = alquilerEquiposLista;
  }

  public void agregarAlquilerAccesorios(AlquilerAccesorios alquilerAccesorios) {
    this.alquilerAccesoriosLista.add(alquilerAccesorios);
  }

  public void agregarAlquilerEquipos(AlquilerEquipos alquilerEquipos) {
    this.alquilerEquiposLista.add(alquilerEquipos);
  }

  public boolean removerAlquilerAccesorios(int idAlquiler) {
    return this.alquilerAccesoriosLista.removeIf(objeto -> objeto.getIdAlquiler() == idAlquiler);
  }

  public boolean removerAlquilerEquipos(int idAlquiler) {
    return this.alquilerEquiposLista.removeIf(objeto -> objeto.getIdAlquiler() == idAlquiler);
  }
}