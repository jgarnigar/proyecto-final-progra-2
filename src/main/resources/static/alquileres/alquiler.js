const API_URL = '/api/alquileres';

let clientesMap = {};
let equiposMap = {};
let accesoriosMap = {};
let alquileresCache = [];

document.addEventListener('DOMContentLoaded', function () {
    cargarAlquileres();
    cargarCatalogos();
});

function cargarCatalogos() {
    Promise.allSettled([
        fetch('/api/clientes').then(function (r) { return r.ok ? r.json() : []; }),
        fetch('/api/equipos').then(function (r) { return r.ok ? r.json() : []; }),
        fetch('/api/accesorios').then(function (r) { return r.ok ? r.json() : []; })
    ]).then(function (resultados) {
        var clientes = resultados[0].status === 'fulfilled' && Array.isArray(resultados[0].value) ? resultados[0].value : [];
        var equipos = resultados[1].status === 'fulfilled' && Array.isArray(resultados[1].value) ? resultados[1].value : [];
        var accesorios = resultados[2].status === 'fulfilled' && Array.isArray(resultados[2].value) ? resultados[2].value : [];

        llenarSelectClientes(clientes);
        llenarChecklistEquipos(equipos);
        llenarListaAccesorios(accesorios);

        if (alquileresCache.length > 0) {
            renderizarTablaAlquileres(alquileresCache);
        }
    });
}

function llenarSelectClientes(clientes) {
    var select = document.getElementById('idClienteAlquiler');
    if (!select) return;
    clientesMap = {};

    if (!clientes || clientes.length === 0) {
        select.innerHTML = '<option value="">No hay clientes registrados</option>';
        return;
    }

    var html = '<option value="">Seleccione un cliente...</option>';
    clientes.forEach(function (c) {
        clientesMap[c.idCliente] = c.nombre;
        var saldo = Number(c.saldoPendiente || 0);
        var inactivo = c.estado === 'INACTIVO';
        var bloqueo = inactivo || saldo > 0;
        var etiquetaExtra = inactivo
            ? ' [INACTIVO]'
            : (saldo > 0 ? ' [Saldo pendiente: Q ' + saldo.toFixed(2) + ']' : '');

        html += '<option value="' + c.idCliente + '"' + (bloqueo ? ' disabled' : '') + '>' +
            '#' + c.idCliente + ' - ' + c.nombre + etiquetaExtra +
            '</option>';
    });
    select.innerHTML = html;
}

function llenarChecklistEquipos(equipos) {
    var contenedor = document.getElementById('contenedorEquiposAlquiler');
    if (!contenedor) return;
    equiposMap = {};

    if (!equipos || equipos.length === 0) {
        contenedor.innerHTML = '<span class="text-muted small">No hay equipos registrados.</span>';
        return;
    }

    equipos.forEach(function (eq) {
        equiposMap[eq.idEquipo] = '#' + eq.idEquipo + ' ' + eq.tipo + ' ' + eq.modelo;
    });

    var disponibles = equipos.filter(function (eq) { return eq.estado === 'DISPONIBLE'; });
    if (disponibles.length === 0) {
        contenedor.innerHTML = '<span class="text-danger small">No hay equipos disponibles en este momento.</span>';
        return;
    }

    var html = '';
    disponibles.forEach(function (eq) {
        var bateria = eq.bateriaPorcentaje !== null && eq.bateriaPorcentaje !== undefined
            ? ' | Bat: ' + eq.bateriaPorcentaje + '%'
            : '';
        html +=
            '<div class="form-check mb-1">' +
            '<input class="form-check-input check-equipo-alquiler" type="checkbox" value="' + eq.idEquipo + '" id="chkEq_' + eq.idEquipo + '">' +
            '<label class="form-check-label" for="chkEq_' + eq.idEquipo + '">' +
            '#' + eq.idEquipo + ' - ' + eq.tipo + ' ' + eq.modelo +
            ' (Q ' + Number(eq.tarifaHora || 0).toFixed(2) + '/h' + bateria + ')' +
            '</label>' +
            '</div>';
    });
    contenedor.innerHTML = html;
}

function llenarListaAccesorios(accesorios) {
    var contenedor = document.getElementById('contenedorAccesoriosAlquiler');
    if (!contenedor) return;
    accesoriosMap = {};

    if (!accesorios || accesorios.length === 0) {
        contenedor.innerHTML = '<span class="text-muted small">No hay accesorios registrados.</span>';
        return;
    }

    accesorios.forEach(function (acc) {
        accesoriosMap[acc.idAccesorio] = acc.nombre;
    });

    var conStock = accesorios.filter(function (acc) { return acc.existenciaActual > 0; });
    if (conStock.length === 0) {
        contenedor.innerHTML = '<span class="text-muted small">Sin accesorios en stock.</span>';
        return;
    }

    var html = '';
    conStock.forEach(function (acc) {
        html +=
            '<div class="d-flex justify-content-between align-items-center mb-2">' +
            '<span>#' + acc.idAccesorio + ' - ' + acc.nombre + ' (Q ' + Number(acc.tarifaFija || 0).toFixed(2) + ' | Disp: ' + acc.existenciaActual + ')</span>' +
            '<input type="number" class="form-control form-control-sm input-acc-alquiler" style="width: 80px;" ' +
            'data-id="' + acc.idAccesorio + '" min="0" max="' + acc.existenciaActual + '" value="0">' +
            '</div>';
    });
    contenedor.innerHTML = html;
}

function cargarAlquileres() {
    fetch(API_URL)
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (msg) {
                    throw new Error(msg || 'No se pudieron cargar los alquileres.');
                });
            }
            return response.json();
        })
        .then(function (alquileres) {
            alquileresCache = Array.isArray(alquileres) ? alquileres : [];
            renderizarTablaAlquileres(alquileresCache);
        })
        .catch(function (error) {
            console.error('Error cargando alquileres:', error);
            var tbody = document.getElementById('cuerpoTablaAlquileres');
            if (tbody) {
                tbody.innerHTML =
                    '<tr>' +
                    '<td colspan="7" class="text-center text-danger py-4">' +
                    'Error de conexión con el servidor: ' + error.message +
                    '</td>' +
                    '</tr>';
            }
        });
}

function renderizarTablaAlquileres(alquileres) {
    var tbody = document.getElementById('cuerpoTablaAlquileres');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (alquileres.length === 0) {
        tbody.innerHTML =
            '<tr>' +
            '<td colspan="7" class="text-center text-muted py-4">' +
            'No hay alquileres registrados. Presiona "Nuevo Alquiler" para crear uno.' +
            '</td>' +
            '</tr>';
        return;
    }

    alquileres.forEach(function (alq) {
        var badgeColor = 'bg-secondary';
        var botonAccion = '<span class="text-muted small">Completado</span>';

        if (alq.estado === 'RESERVADO') {
            badgeColor = 'bg-info text-dark';
            botonAccion =
                '<button type="button" class="btn btn-sm btn-success" onclick="entregarAlquiler(' + alq.idAlquiler + ')">' +
                '<i class="bi bi-box-arrow-right me-1"></i> Entregar' +
                '</button>';
        } else if (alq.estado === 'ACTIVO') {
            badgeColor = 'bg-warning text-dark';
            botonAccion =
                '<button type="button" class="btn btn-sm btn-primary" onclick="abrirModalDevolucion(' + alq.idAlquiler + ')">' +
                '<i class="bi bi-cash-coin me-1"></i> Recibir / Cobrar' +
                '</button>';
        } else if (alq.estado === 'FINALIZADO') {
            badgeColor = 'bg-success';
        }

        var nombreCliente = clientesMap[alq.idCliente]
            ? '#' + alq.idCliente + ' - ' + clientesMap[alq.idCliente]
            : 'Cliente #' + alq.idCliente;

        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td class="fw-bold">#' + alq.idAlquiler + '</td>' +
            '<td>' + nombreCliente + '</td>' +
            '<td>Q ' + Number(alq.depositoCobrado || 0).toFixed(2) + '</td>' +
            '<td>' + formatearFecha(alq.fechaEstimada) + '</td>' +
            '<td>' + formatearFecha(alq.fechaReal) + '</td>' +
            '<td><span class="badge ' + badgeColor + '">' + alq.estado + '</span></td>' +
            '<td>' + botonAccion + '</td>';
        tbody.appendChild(tr);
    });
}

function abrirModalNuevoAlquiler() {
    var form = document.getElementById('formNuevoAlquiler');
    if (form) form.reset();
    var alerta = document.getElementById('alertaNuevoAlquiler');
    if (alerta) alerta.classList.add('d-none');

    cargarCatalogos();

    var modalEl = document.getElementById('modalNuevoAlquiler');
    if (modalEl) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
}

function crearAlquiler(event) {
    event.preventDefault();
    var alerta = document.getElementById('alertaNuevoAlquiler');
    alerta.classList.add('d-none');

    var idCliente = parseInt(document.getElementById('idClienteAlquiler').value);
    var fechaEstimada = document.getElementById('fechaEstimadaAlquiler').value;

    if (isNaN(idCliente)) {
        alerta.textContent = 'Debe seleccionar un cliente válido.';
        alerta.classList.remove('d-none');
        return;
    }

    var alquilerEquiposLista = [];
    document.querySelectorAll('.check-equipo-alquiler:checked').forEach(function (chk) {
        alquilerEquiposLista.push({ idEquipo: parseInt(chk.value) });
    });

    if (alquilerEquiposLista.length === 0) {
        alerta.textContent = 'Debe seleccionar al menos un equipo disponible.';
        alerta.classList.remove('d-none');
        return;
    }

    var alquilerAccesoriosLista = [];
    document.querySelectorAll('.input-acc-alquiler').forEach(function (inp) {
        var cantidad = parseInt(inp.value || '0');
        if (cantidad > 0) {
            alquilerAccesoriosLista.push({
                idAccesorio: parseInt(inp.dataset.id),
                cantidad: cantidad,
                unidadesExtraviadas: 0
            });
        }
    });

    var payload = {
        idCliente: idCliente,
        fechaEstimada: fechaEstimada,
        alquilerEquiposLista: alquilerEquiposLista,
        alquilerAccesoriosLista: alquilerAccesoriosLista
    };

    fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (errorMsg) {
                    throw new Error(errorMsg || 'No se pudo registrar la reserva.');
                });
            }
            return response.json();
        })
        .then(function (nuevo) {
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalNuevoAlquiler')).hide();
            document.getElementById('formNuevoAlquiler').reset();
            mostrarAlertaGeneral('Reserva #' + nuevo.idAlquiler + ' creada correctamente.', 'success');
            cargarAlquileres();
            cargarCatalogos();
        })
        .catch(function (error) {
            alerta.textContent = error.message;
            alerta.classList.remove('d-none');
        });
}

function entregarAlquiler(idAlquiler) {
    if (!confirm('¿Confirmar entrega del alquiler #' + idAlquiler + '? Se calculará el depósito y los equipos pasarán a ALQUILADO.')) {
        return;
    }

    fetch(API_URL + '/' + idAlquiler + '/entregar', {
        method: 'PUT'
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (errorMsg) {
                    throw new Error(errorMsg || 'No se pudo entregar el alquiler.');
                });
            }
            return response.json();
        })
        .then(function (res) {
            mostrarAlertaGeneral(
                'Alquiler #' + idAlquiler + ' entregado (ACTIVO). Depósito cobrado: Q ' + Number(res.depositoCobrado).toFixed(2) + '.',
                'success'
            );
            cargarAlquileres();
            cargarCatalogos();
        })
        .catch(function (error) {
            mostrarAlertaGeneral(error.message, 'danger');
        });
}

function alternarCamposPago() {
    var metodo = document.getElementById('devMetodoPago').value;
    document.getElementById('bloquePagoEfectivo').classList.toggle('d-none', metodo !== 'EFECTIVO');
    document.getElementById('bloquePagoTarjeta').classList.toggle('d-none', metodo !== 'TARJETA');
}

function abrirModalDevolucion(idAlquiler) {
    document.getElementById('formDevolucion').reset();
    document.getElementById('alertaDevolucion').classList.add('d-none');
    document.getElementById('devolucionIdAlquiler').value = idAlquiler;
    document.getElementById('lblDevolucionId').innerText = idAlquiler;
    alternarCamposPago();

    var alquiler = alquileresCache.find(function (a) { return a.idAlquiler === idAlquiler; });
    var contEq = document.getElementById('contenedorDevEquipos');
    var contAcc = document.getElementById('contenedorDevAccesorios');

    if (contEq) {
        if (alquiler && alquiler.alquilerEquiposLista && alquiler.alquilerEquiposLista.length > 0) {
            contEq.innerHTML = alquiler.alquilerEquiposLista.map(function (ae) {
                var desc = equiposMap[ae.idEquipo] || ('Equipo #' + ae.idEquipo);
                return '<div class="form-check">' +
                    '<input class="form-check-input check-dev-danado" type="checkbox" value="' + ae.idEquipo + '" id="devEq_' + ae.idEquipo + '">' +
                    '<label class="form-check-label" for="devEq_' + ae.idEquipo + '">' + desc + ' (Marcar como DAÑADO)</label>' +
                    '</div>';
            }).join('');
        } else {
            contEq.innerHTML = '<span class="text-muted small">Sin equipos asociados.</span>';
        }
    }

    if (contAcc) {
        if (alquiler && alquiler.alquilerAccesoriosLista && alquiler.alquilerAccesoriosLista.length > 0) {
            contAcc.innerHTML = alquiler.alquilerAccesoriosLista.map(function (aa) {
                var nombre = accesoriosMap[aa.idAccesorio] || ('Accesorio #' + aa.idAccesorio);
                return '<div class="d-flex justify-content-between align-items-center mb-1">' +
                    '<span>' + nombre + ' (Alquilados: ' + aa.cantidad + ')</span>' +
                    '<input type="number" class="form-control form-control-sm input-dev-extraviado" style="width: 75px;" ' +
                    'data-id="' + aa.idAccesorio + '" data-cant="' + aa.cantidad + '" min="0" max="' + aa.cantidad + '" value="0">' +
                    '</div>';
            }).join('');
        } else {
            contAcc.innerHTML = '<span class="text-muted small">Sin accesorios asociados.</span>';
        }
    }

    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalDevolucion')).show();
}

function procesarDevolucion(event) {
    event.preventDefault();
    var alerta = document.getElementById('alertaDevolucion');
    alerta.classList.add('d-none');

    var idAlquiler = document.getElementById('devolucionIdAlquiler').value;
    var metodoPago = document.getElementById('devMetodoPago').value;

    var equiposDevueltos = [];
    document.querySelectorAll('.check-dev-danado:checked').forEach(function (chk) {
        equiposDevueltos.push({
            idEquipo: parseInt(chk.value),
            estadoDevolucion: 'DAÑADO'
        });
    });

    var accesoriosDevueltos = [];
    document.querySelectorAll('.input-dev-extraviado').forEach(function (inp) {
        var extraviadas = parseInt(inp.value || '0');
        if (extraviadas > 0) {
            accesoriosDevueltos.push({
                idAccesorio: parseInt(inp.dataset.id),
                cantidad: parseInt(inp.dataset.cant),
                unidadesExtraviadas: extraviadas
            });
        }
    });

    var payload = {
        alquilerEquiposLista: equiposDevueltos,
        alquilerAccesoriosLista: accesoriosDevueltos,
        metodoPago: metodoPago,
        montoRecibido: metodoPago === 'EFECTIVO' ? parseFloat(document.getElementById('devMontoRecibido').value || 0) : null,
        tarjetaAprobada: metodoPago === 'TARJETA' ? document.getElementById('devTarjetaAprobada').value === 'true' : null
    };

    fetch(API_URL + '/' + idAlquiler + '/devolver', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (errorMsg) {
                    throw new Error(errorMsg);
                });
            }
            return response.json();
        })
        .then(function (res) {
            var resumen =
                'Alquiler #' + idAlquiler + ' finalizado.\n\n' +
                'Uso equipos (H): Q ' + Number(res.totalUsoEquipos).toFixed(2) + '\n' +
                'Accesorios (A): Q ' + Number(res.totalAccesorios).toFixed(2) + '\n' +
                'Recargos retraso (R): Q ' + Number(res.totalRecargos).toFixed(2) + '\n' +
                'Daños / Extravíos (D): Q ' + Number(res.totalDanos).toFixed(2) + '\n' +
                '----------------------------------\n' +
                'Total cobrado en caja: Q ' + Number(res.totalCobradoCaja).toFixed(2) + '\n';

            if (res.metodoPago === 'EFECTIVO') {
                resumen += 'Cambio a entregar: Q ' + Number(res.cambioDevuelto).toFixed(2) + '\n';
            } else {
                resumen += 'Referencia Tarjeta: ' + res.numeroReferencia + '\n';
            }

            if (Number(res.excedenteDepositoDevuelto) > 0) {
                resumen += 'Excedente depósito a devolver al cliente: Q ' + Number(res.excedenteDepositoDevuelto).toFixed(2) + '\n';
            }
            if (Number(res.saldoAgregadoCliente) > 0) {
                resumen += 'Saldo agregado a deuda del cliente: Q ' + Number(res.saldoAgregadoCliente).toFixed(2) + '\n';
            }

            alert(resumen);
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalDevolucion')).hide();
            document.getElementById('formDevolucion').reset();
            cargarAlquileres();
            cargarCatalogos();
        })
        .catch(function (error) {
            alerta.textContent = error.message;
            alerta.className = 'alert alert-danger';
            alerta.classList.remove('d-none');
        });
}

function formatearFecha(fechaStr) {
    if (!fechaStr) return '-';
    return new Date(fechaStr).toLocaleString();
}

function mostrarAlertaGeneral(mensaje, tipo) {
    var alerta = document.getElementById('alertaGeneral');
    if (!alerta) return;
    alerta.textContent = mensaje;
    alerta.className = 'alert alert-' + tipo;
    alerta.classList.remove('d-none');
    setTimeout(function () { alerta.classList.add('d-none'); }, 5000);
}