package com.proyectofinal.service;

import com.proyectofinal.DAO.*;
import com.proyectofinal.modelo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AlquilerService {

    private final AlquilerDAO alquilerDAO;
    private final ClientesDAO clientesDAO;
    private final AccesoriosDAO accesoriosDAO;
    private final EquiposDAO equiposDAO;
    private final PagoDAO pagoDAO;

    public AlquilerService(AlquilerDAO alquilerDAO, ClientesDAO clientesDAO, AccesoriosDAO accesoriosDAO, EquiposDAO equiposDAO, PagoDAO pagoDAO) {
        this.alquilerDAO = alquilerDAO;
        this.clientesDAO = clientesDAO;
        this.accesoriosDAO = accesoriosDAO;
        this.equiposDAO = equiposDAO;
        this.pagoDAO = pagoDAO;
    }

    public List<Alquileres> obtenerTodos() {
        return alquilerDAO.obtenerTodos();
    }

    public Alquileres procesarNuevoAlquiler(Alquileres alquiler) {
        Clientes cliente = clientesDAO.consultar(alquiler.getIdCliente());
        if (cliente == null || "INACTIVO".equals(cliente.getEstado())) {
            throw new IllegalArgumentException("El cliente no existe o se encuentra inactivo.");
        }

        if (cliente.getSaldoPendiente() != null && cliente.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("El cliente tiene saldo pendiente y no puede iniciar un nuevo alquiler.");
        }

        alquiler.setEstado("RESERVADO");
        alquiler.setFechaReal(null);
        alquiler.setDepositoCobrado(BigDecimal.ZERO);

        if (alquiler.getFechaEstimada() == null || alquiler.getFechaEstimada().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha estimada de devolución debe ser futura.");
        }

        boolean registrado = alquilerDAO.registrarAlquiler(alquiler);
        if (!registrado) {
            throw new RuntimeException("Error interno: No se pudo guardar el alquiler en la base de datos.");
        }

        return alquiler;
    }

    @Transactional
    public Alquileres entregarAlquiler(int idAlquiler) {
        Alquileres alquiler = alquilerDAO.consultar(idAlquiler);
        if (alquiler == null) {
            throw new IllegalArgumentException("No se encontró el alquiler con ID: " + idAlquiler);
        }

        if (!"RESERVADO".equals(alquiler.getEstado())) {
            throw new IllegalStateException("Solo los alquileres en estado RESERVADO pueden entregarse (pasar a ACTIVO).");
        }

        BigDecimal depositoTotal = BigDecimal.ZERO;

        if (alquiler.getAlquilerEquiposLista() != null) {
            for (AlquilerEquipos ae : alquiler.getAlquilerEquiposLista()) {
                Equipos equipo = equiposDAO.consultar(ae.getIdEquipo());
                if (equipo == null) {
                    throw new RuntimeException("Error: No se encontró el equipo con ID " + ae.getIdEquipo());
                }

                boolean esScooter = equipo.getTipo().toLowerCase().contains("scooter") ||
                        equipo.getModelo().toLowerCase().contains("scooter");

                if (esScooter) {
                    if (equipo.getBateriaPorcentaje() < 30) {
                        throw new IllegalStateException("El scooter " + equipo.getIdEquipo() +
                                " tiene menos del 30% de batería (" + equipo.getBateriaPorcentaje() +
                                "%). No puede ser alquilado.");
                    }
                    depositoTotal = depositoTotal.add(new BigDecimal("200.00"));
                } else {
                    depositoTotal = depositoTotal.add(new BigDecimal("100.00"));
                }

                equiposDAO.actualizarEstado(equipo.getIdEquipo(), "ALQUILADO");
            }
        }

        if (alquiler.getAlquilerAccesoriosLista() != null) {
            for (AlquilerAccesorios aa : alquiler.getAlquilerAccesoriosLista()) {
                Accesorios accesorio = accesoriosDAO.consultar(aa.getIdAccesorio());
                if (accesorio == null) {
                    throw new RuntimeException("Accesorio no encontrado: " + aa.getIdAccesorio());
                }

                if (accesorio.getExistenciaActual() < aa.getCantidad()) {
                    throw new IllegalStateException("No hay stock suficiente para el accesorio: " + accesorio.getNombre());
                }

                accesorio.setExistenciaActual(accesorio.getExistenciaActual() - aa.getCantidad());
                accesoriosDAO.modificar(accesorio);
            }
        }

        alquiler.setEstado("ACTIVO");
        alquiler.setDepositoCobrado(depositoTotal);
        alquiler.setFechaReal(LocalDateTime.now()); // Hora exacta de entrega/salida

        boolean modificado = alquilerDAO.modificar(alquiler);
        if (!modificado) {
            throw new RuntimeException("Error al actualizar el estado del alquiler en la base de datos.");
        }

        return alquiler;
    }

    @Transactional
    public Alquileres devolverAlquiler(int idAlquiler, Alquileres datosDevolucion) {
        Alquileres alquilerBD = alquilerDAO.consultar(idAlquiler);
        if (alquilerBD == null) {
            throw new IllegalArgumentException("No se encontró el alquiler con ID: " + idAlquiler);
        }

        if (!"ACTIVO".equals(alquilerBD.getEstado())) {
            throw new IllegalStateException("Solo los alquileres en estado ACTIVO pueden ser devueltos.");
        }

        LocalDateTime fechaEntrega = alquilerBD.getFechaReal() != null ? alquilerBD.getFechaReal() : LocalDateTime.now();
        LocalDateTime fechaDevolucion = LocalDateTime.now();

        // Redondeo de horas
        long minutosUso = Math.max(0, Duration.between(fechaEntrega, fechaDevolucion).toMinutes());
        long horasUso = Math.max(1, (long) Math.ceil(minutosUso / 60.0));

        // 15 minutos de penalizacion
        long minutosRetraso = Duration.between(alquilerBD.getFechaEstimada(), fechaDevolucion).toMinutes();
        long horasRetraso = (minutosRetraso > 15) ? (long) Math.ceil(minutosRetraso / 60.0) : 0;

        BigDecimal sumaUsoEquipos = BigDecimal.ZERO;
        BigDecimal sumaRecargos = BigDecimal.ZERO;
        BigDecimal sumaDanos = BigDecimal.ZERO;
        BigDecimal sumaAccesorios = BigDecimal.ZERO;

        // calculo de uso, dano, horas y tarifa
        if (alquilerBD.getAlquilerEquiposLista() != null) {
            for (AlquilerEquipos aeBD : alquilerBD.getAlquilerEquiposLista()) {
                Equipos equipoBD = equiposDAO.consultar(aeBD.getIdEquipo());

                BigDecimal costoUso = equipoBD.getTarifaHora().multiply(BigDecimal.valueOf(horasUso));
                sumaUsoEquipos = sumaUsoEquipos.add(costoUso);

                if (horasRetraso > 0) {
                    BigDecimal recargo = equipoBD.getTarifaHora()
                            .multiply(BigDecimal.valueOf(horasRetraso))
                            .multiply(new BigDecimal("1.5"));
                    sumaRecargos = sumaRecargos.add(recargo);
                }

                String estadoDev = "BUENO";
                if (datosDevolucion.getAlquilerEquiposLista() != null) {
                    for (AlquilerEquipos eqReq : datosDevolucion.getAlquilerEquiposLista()) {
                        if (eqReq.getIdEquipo() == aeBD.getIdEquipo() && eqReq.getEstadoDevolucion() != null) {
                            estadoDev = eqReq.getEstadoDevolucion();
                            break;
                        }
                    }
                }

                aeBD.setEstadoDevolucion(estadoDev);

                if ("DAÑADO".equalsIgnoreCase(estadoDev)) {
                    sumaDanos = sumaDanos.add(equipoBD.getPrecioReposicion());
                    equiposDAO.actualizarEstado(equipoBD.getIdEquipo(), "EN MANTENIMIENTO");
                } else {
                    equiposDAO.actualizarEstado(equipoBD.getIdEquipo(), "DISPONIBLE");
                }
            }
        }

        // caculo de accesorios y si son extraviados
        if (alquilerBD.getAlquilerAccesoriosLista() != null) {
            for (AlquilerAccesorios aaBD : alquilerBD.getAlquilerAccesoriosLista()) {
                Accesorios accesorioBD = accesoriosDAO.consultar(aaBD.getIdAccesorio());

                BigDecimal tarifaAcc = accesorioBD.getTarifaFija().multiply(BigDecimal.valueOf(aaBD.getCantidad()));
                sumaAccesorios = sumaAccesorios.add(tarifaAcc);

                int extraviadas = 0;
                if (datosDevolucion.getAlquilerAccesoriosLista() != null) {
                    for (AlquilerAccesorios accReq : datosDevolucion.getAlquilerAccesoriosLista()) {
                        if (accReq.getIdAccesorio() == aaBD.getIdAccesorio()) {
                            extraviadas = Math.max(0, Math.min(aaBD.getCantidad(), accReq.getUnidadesExtraviadas()));
                            break;
                        }
                    }
                }

                aaBD.setUnidadesExtraviadas(extraviadas);

                int devueltosEnBuenEstado = aaBD.getCantidad() - extraviadas;
                if (devueltosEnBuenEstado > 0) {
                    accesorioBD.setExistenciaActual(accesorioBD.getExistenciaActual() + devueltosEnBuenEstado);
                    accesoriosDAO.modificar(accesorioBD);
                }

                if (extraviadas > 0) {
                    BigDecimal cobroPerdida = accesorioBD.getPrecioReposicion().multiply(BigDecimal.valueOf(extraviadas));
                    sumaDanos = sumaDanos.add(cobroPerdida);
                }
            }
        }

        BigDecimal deposito = alquilerBD.getDepositoCobrado() != null ? alquilerBD.getDepositoCobrado() : BigDecimal.ZERO;
        BigDecimal excedenteDeposito = BigDecimal.ZERO;
        BigDecimal saldoAgregado = BigDecimal.ZERO;

        if (sumaDanos.compareTo(deposito) > 0) {
            saldoAgregado = sumaDanos.subtract(deposito);
        } else {
            excedenteDeposito = deposito.subtract(sumaDanos);
        }

        // tal a pagar en caja: Σ(H × tarifa) + A + R
        BigDecimal totalCobrarCaja = sumaUsoEquipos.add(sumaAccesorios).add(sumaRecargos).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalGeneral = totalCobrarCaja.add(sumaDanos).setScale(2, RoundingMode.HALF_UP);

        //pago en Efectivo o Tarjeta
        String metodoPago = datosDevolucion.getMetodoPago() != null ? datosDevolucion.getMetodoPago().trim().toUpperCase() : "";
        BigDecimal cambio = BigDecimal.ZERO;
        String referencia = null;

        if ("EFECTIVO".equals(metodoPago)) {
            BigDecimal recibido = datosDevolucion.getMontoRecibido() != null ? datosDevolucion.getMontoRecibido() : BigDecimal.ZERO;
            if (recibido.compareTo(totalCobrarCaja) < 0) {
                throw new IllegalStateException("Monto en efectivo insuficiente. Total a cobrar en caja: Q " + totalCobrarCaja);
            }
            cambio = recibido.subtract(totalCobrarCaja).setScale(2, RoundingMode.HALF_UP);
            referencia = "EFECTIVO";
        } else if ("TARJETA".equals(metodoPago)) {
            if (datosDevolucion.getTarjetaAprobada() == null || !datosDevolucion.getTarjetaAprobada()) {
                throw new IllegalStateException("Pago con tarjeta rechazado por el banco emisor. Puede reintentar el cobro.");
            }
            referencia = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else {
            throw new IllegalArgumentException("Debe seleccionar un método de pago válido (EFECTIVO o TARJETA).");
        }

        // Actualizar saldo pendiente del cliente (solo si D > Depósito)
        if (saldoAgregado.compareTo(BigDecimal.ZERO) > 0) {
            Clientes cliente = clientesDAO.consultar(alquilerBD.getIdCliente());
            BigDecimal saldoActual = cliente.getSaldoPendiente() != null ? cliente.getSaldoPendiente() : BigDecimal.ZERO;
            cliente.setSaldoPendiente(saldoActual.add(saldoAgregado));
            clientesDAO.modificar(cliente);
        }

        alquilerBD.setFechaReal(fechaDevolucion);
        alquilerBD.setEstado("FINALIZADO");
        alquilerDAO.modificar(alquilerBD);

        pagoDAO.registrarPago(idAlquiler, fechaDevolucion, metodoPago, totalCobrarCaja, referencia);

        // Dsglose para mostrar recibo en pantalla
        alquilerBD.setMetodoPago(metodoPago);
        alquilerBD.setNumeroReferencia(referencia);
        alquilerBD.setTotalUsoEquipos(sumaUsoEquipos.setScale(2, RoundingMode.HALF_UP));
        alquilerBD.setTotalAccesorios(sumaAccesorios.setScale(2, RoundingMode.HALF_UP));
        alquilerBD.setTotalRecargos(sumaRecargos.setScale(2, RoundingMode.HALF_UP));
        alquilerBD.setTotalDanos(sumaDanos.setScale(2, RoundingMode.HALF_UP));
        alquilerBD.setTotalGeneral(totalGeneral);
        alquilerBD.setExcedenteDepositoDevuelto(excedenteDeposito.setScale(2, RoundingMode.HALF_UP));
        alquilerBD.setSaldoAgregadoCliente(saldoAgregado.setScale(2, RoundingMode.HALF_UP));
        alquilerBD.setTotalCobradoCaja(totalCobrarCaja);
        alquilerBD.setCambioDevuelto(cambio);

        return alquilerBD;
    }
}