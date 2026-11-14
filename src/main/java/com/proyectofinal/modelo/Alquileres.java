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

  private List<AlquilerAccesorios> alquilerAccesoriosLista = new ArrayList<>();
  private List<AlquilerEquipos> alquilerEquiposLista = new ArrayList<>();

  // Campos para procesar el pago en devolución
  private String metodoPago; // "EFECTIVO" o "TARJETA"
  private BigDecimal montoRecibido;
  private Boolean tarjetaAprobada;

  // Campos de respuesta con el desglose del cálculo
  private String numeroReferencia;
  private BigDecimal totalUsoEquipos;
  private BigDecimal totalAccesorios;
  private BigDecimal totalRecargos;
  private BigDecimal totalDanos;
  private BigDecimal totalGeneral;
  private BigDecimal excedenteDepositoDevuelto;
  private BigDecimal saldoAgregadoCliente;
  private BigDecimal totalCobradoCaja;
  private BigDecimal cambioDevuelto;

  public int getIdAlquiler() { return idAlquiler; }
  public void setIdAlquiler(int idAlquiler) { this.idAlquiler = idAlquiler; }

  public int getIdCliente() { return idCliente; }
  public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

  public BigDecimal getDepositoCobrado() { return depositoCobrado; }
  public void setDepositoCobrado(BigDecimal depositoCobrado) { this.depositoCobrado = depositoCobrado; }

  public String getEstado() { return estado; }
  public void setEstado(String estado) { this.estado = estado; }

  public LocalDateTime getFechaEstimada() { return fechaEstimada; }
  public void setFechaEstimada(LocalDateTime fechaEstimada) { this.fechaEstimada = fechaEstimada; }

  public LocalDateTime getFechaReal() { return fechaReal; }
  public void setFechaReal(LocalDateTime fechaReal) { this.fechaReal = fechaReal; }

  public List<AlquilerAccesorios> getAlquilerAccesoriosLista() { return alquilerAccesoriosLista; }
  public void setAlquilerAccesoriosLista(List<AlquilerAccesorios> alquilerAccesoriosLista) { this.alquilerAccesoriosLista = alquilerAccesoriosLista; }

  public List<AlquilerEquipos> getAlquilerEquiposLista() { return alquilerEquiposLista; }
  public void setAlquilerEquiposLista(List<AlquilerEquipos> alquilerEquiposLista) { this.alquilerEquiposLista = alquilerEquiposLista; }

  public String getMetodoPago() { return metodoPago; }
  public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

  public BigDecimal getMontoRecibido() { return montoRecibido; }
  public void setMontoRecibido(BigDecimal montoRecibido) { this.montoRecibido = montoRecibido; }

  public Boolean getTarjetaAprobada() { return tarjetaAprobada; }
  public void setTarjetaAprobada(Boolean tarjetaAprobada) { this.tarjetaAprobada = tarjetaAprobada; }

  public String getNumeroReferencia() { return numeroReferencia; }
  public void setNumeroReferencia(String numeroReferencia) { this.numeroReferencia = numeroReferencia; }

  public BigDecimal getTotalUsoEquipos() { return totalUsoEquipos; }
  public void setTotalUsoEquipos(BigDecimal totalUsoEquipos) { this.totalUsoEquipos = totalUsoEquipos; }

  public BigDecimal getTotalAccesorios() { return totalAccesorios; }
  public void setTotalAccesorios(BigDecimal totalAccesorios) { this.totalAccesorios = totalAccesorios; }

  public BigDecimal getTotalRecargos() { return totalRecargos; }
  public void setTotalRecargos(BigDecimal totalRecargos) { this.totalRecargos = totalRecargos; }

  public BigDecimal getTotalDanos() { return totalDanos; }
  public void setTotalDanos(BigDecimal totalDanos) { this.totalDanos = totalDanos; }

  public BigDecimal getTotalGeneral() { return totalGeneral; }
  public void setTotalGeneral(BigDecimal totalGeneral) { this.totalGeneral = totalGeneral; }

  public BigDecimal getExcedenteDepositoDevuelto() { return excedenteDepositoDevuelto; }
  public void setExcedenteDepositoDevuelto(BigDecimal excedenteDepositoDevuelto) { this.excedenteDepositoDevuelto = excedenteDepositoDevuelto; }

  public BigDecimal getSaldoAgregadoCliente() { return saldoAgregadoCliente; }
  public void setSaldoAgregadoCliente(BigDecimal saldoAgregadoCliente) { this.saldoAgregadoCliente = saldoAgregadoCliente; }

  public BigDecimal getTotalCobradoCaja() { return totalCobradoCaja; }
  public void setTotalCobradoCaja(BigDecimal totalCobradoCaja) { this.totalCobradoCaja = totalCobradoCaja; }

  public BigDecimal getCambioDevuelto() { return cambioDevuelto; }
  public void setCambioDevuelto(BigDecimal cambioDevuelto) { this.cambioDevuelto = cambioDevuelto; }

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