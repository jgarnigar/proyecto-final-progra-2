package com.proyectofinal.service;

import com.proyectofinal.DAO.AlquilerDAO;
import com.proyectofinal.DAO.ClientesDAO;
import com.proyectofinal.DAO.AccesoriosDAO;
import com.proyectofinal.DAO.EquiposDAO;
import com.proyectofinal.modelo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AlquilerService {

    private final AlquilerDAO alquilerDAO;
    private final ClientesDAO clientesDAO;
    private final AccesoriosDAO accesoriosDAO;
    private final EquiposDAO equiposDAO;

    public AlquilerService(AlquilerDAO alquilerDAO, ClientesDAO clientesDAO, AccesoriosDAO accesoriosDAO, EquiposDAO equiposDAO) {
        this.alquilerDAO = alquilerDAO;
        this.clientesDAO = clientesDAO;
        this.accesoriosDAO = accesoriosDAO;
        this.equiposDAO = equiposDAO;
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

        LocalDateTime fechaReal = LocalDateTime.now();
        alquilerBD.setFechaReal(fechaReal);
        alquilerBD.setEstado("FINALIZADO");

        BigDecimal totalAPagar = BigDecimal.ZERO;

        long horasRetraso = java.time.Duration.between(alquilerBD.getFechaEstimada(), fechaReal).toHours();
        if (horasRetraso < 0) horasRetraso = 0;

        if (datosDevolucion.getAlquilerEquiposLista() != null) {
            for (AlquilerEquipos equipoDevuelto : datosDevolucion.getAlquilerEquiposLista()) {
                Equipos equipoBD = equiposDAO.consultar(equipoDevuelto.getIdEquipo());

                BigDecimal recargoRetraso = equipoBD.getTarifaHora().multiply(new BigDecimal(horasRetraso));
                recargoRetraso = recargoRetraso.multiply(new BigDecimal("1.5"));
                totalAPagar = totalAPagar.add(recargoRetraso);

                if ("DAÑADO".equalsIgnoreCase(equipoDevuelto.getEstadoDevolucion())) {
                    totalAPagar = totalAPagar.add(equipoBD.getPrecioReposicion());
                    equiposDAO.actualizarEstado(equipoBD.getIdEquipo(), "MANTENIMIENTO");
                } else {
                    equiposDAO.actualizarEstado(equipoBD.getIdEquipo(), "DISPONIBLE");
                }

                for (AlquilerEquipos ae : alquilerBD.getAlquilerEquiposLista()) {
                    if (ae.getIdEquipo() == equipoDevuelto.getIdEquipo()) {
                        ae.setEstadoDevolucion(equipoDevuelto.getEstadoDevolucion());
                    }
                }
            }
        }

        if (datosDevolucion.getAlquilerAccesoriosLista() != null) {
            for (AlquilerAccesorios accesorioDevuelto : datosDevolucion.getAlquilerAccesoriosLista()) {
                Accesorios accesorioBD = accesoriosDAO.consultar(accesorioDevuelto.getIdAccesorio());

                int devueltosEnBuenEstado = accesorioDevuelto.getCantidad() - accesorioDevuelto.getUnidadesExtraviadas();

                if (devueltosEnBuenEstado > 0) {
                    accesorioBD.setExistenciaActual(accesorioBD.getExistenciaActual() + devueltosEnBuenEstado);
                    accesoriosDAO.modificar(accesorioBD);
                }

                if (accesorioDevuelto.getUnidadesExtraviadas() > 0) {
                    BigDecimal cobroPorPerdida = accesorioBD.getPrecioReposicion().multiply(new BigDecimal(accesorioDevuelto.getUnidadesExtraviadas()));
                    totalAPagar = totalAPagar.add(cobroPorPerdida);
                }

                for (AlquilerAccesorios aa : alquilerBD.getAlquilerAccesoriosLista()) {
                    if (aa.getIdAccesorio() == accesorioDevuelto.getIdAccesorio()) {
                        aa.setUnidadesExtraviadas(accesorioDevuelto.getUnidadesExtraviadas());
                    }
                }
            }
        }

        totalAPagar = totalAPagar.subtract(alquilerBD.getDepositoCobrado());

        Clientes cliente = clientesDAO.consultar(alquilerBD.getIdCliente());
        BigDecimal saldoActual = cliente.getSaldoPendiente() != null ? cliente.getSaldoPendiente() : BigDecimal.ZERO;

        cliente.setSaldoPendiente(saldoActual.add(totalAPagar));
        clientesDAO.modificar(cliente);

        alquilerDAO.modificar(alquilerBD);

        return alquilerBD;
    }
}