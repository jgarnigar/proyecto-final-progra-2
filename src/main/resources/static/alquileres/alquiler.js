const API_URL = '/api/alquileres';

document.addEventListener('DOMContentLoaded', () => {
    cargarAlquileres();
});

function cargarAlquileres() {
    fetch(API_URL)
        .then(async response => {
            if (!response.ok) {
                const msg = await response.text();
                throw new Error(msg || 'No se pudieron cargar los alquileres.');
            }
            return response.json();
        })
        .then(alquileres => {
            const tbody = document.getElementById('cuerpoTablaAlquileres');
            tbody.innerHTML = '';

            if (!alquileres || alquileres.length === 0) {
                tbody.innerHTML =
                    '<tr>' +
                    '<td colspan="7" class="text-center text-muted py-4">' +
                    'No hay alquileres registrados. Presiona "Nuevo Alquiler" para crear uno.' +
                    '</td>' +
                    '</tr>';
                return;
            }

            alquileres.forEach(alq => {
                let badgeColor = 'bg-secondary';
                let botonAccion = '<span class="text-muted small">Completado</span>';

                if (alq.estado === 'RESERVADO') {
                    badgeColor = 'bg-info text-dark';
                    botonAccion =
                        '<button class="btn btn-sm btn-success" onclick="entregarAlquiler(' + alq.idAlquiler + ')">' +
                        '<i class="bi bi-box-arrow-right me-1"></i> Entregar' +
                        '</button>';
                } else if (alq.estado === 'ACTIVO') {
                    badgeColor = 'bg-warning text-dark';
                    botonAccion =
                        '<button class="btn btn-sm btn-primary" onclick="abrirModalDevolucion(' + alq.idAlquiler + ')">' +
                        '<i class="bi bi-cash-coin me-1"></i> Recibir / Cobrar' +
                        '</button>';
                } else if (alq.estado === 'FINALIZADO') {
                    badgeColor = 'bg-success';
                }

                const tr = document.createElement('tr');
                tr.innerHTML =
                    '<td class="fw-bold">#' + alq.idAlquiler + '</td>' +
                    '<td>Cliente #' + alq.idCliente + '</td>' +
                    '<td>Q ' + Number(alq.depositoCobrado || 0).toFixed(2) + '</td>' +
                    '<td>' + formatearFecha(alq.fechaEstimada) + '</td>' +
                    '<td>' + formatearFecha(alq.fechaReal) + '</td>' +
                    '<td><span class="badge ' + badgeColor + '">' + alq.estado + '</span></td>' +
                    '<td>' + botonAccion + '</td>';
                tbody.appendChild(tr);
            });
        })
        .catch(error => {
            console.error('Error cargando alquileres:', error);
            const tbody = document.getElementById('cuerpoTablaAlquileres');
            tbody.innerHTML =
                '<tr>' +
                '<td colspan="7" class="text-center text-danger py-4">' +
                'Error de conexión con el servidor: ' + error.message +
                '</td>' +
                '</tr>';
        });
}

function crearAlquiler(event) {
    event.preventDefault();
    const alerta = document.getElementById('alertaNuevoAlquiler');
    alerta.classList.add('d-none');

    const idCliente = parseInt(document.getElementById('idClienteAlquiler').value);
    const fechaEstimada = document.getElementById('fechaEstimadaAlquiler').value;
    const equiposTexto = document.getElementById('equiposAlquiler').value;
    const accesoriosTexto = document.getElementById('accesoriosAlquiler').value;

    const alquilerEquiposLista = equiposTexto
        .split(',')
        .map(item => parseInt(item.trim()))
        .filter(id => !isNaN(id))
        .map(idEquipo => ({ idEquipo }));

    if (alquilerEquiposLista.length === 0) {
        alerta.textContent = 'Debe ingresar al menos un ID de equipo válido.';
        alerta.classList.remove('d-none');
        return;
    }

    const alquilerAccesoriosLista = [];
    if (accesoriosTexto.trim() !== '') {
        accesoriosTexto.split(',').forEach(par => {
            const [idStr, cantStr] = par.split(':');
            const idAccesorio = parseInt((idStr || '').trim());
            const cantidad = parseInt((cantStr || '').trim());
            if (!isNaN(idAccesorio) && !isNaN(cantidad) && cantidad > 0) {
                alquilerAccesoriosLista.push({
                    idAccesorio,
                    cantidad,
                    unidadesExtraviadas: 0
                });
            }
        });
    }

    const payload = {
        idCliente,
        fechaEstimada,
        alquilerEquiposLista,
        alquilerAccesoriosLista
    };

    fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(async response => {
            if (!response.ok) {
                const errorMsg = await response.text();
                throw new Error(errorMsg || 'No se pudo registrar la reserva.');
            }
            return response.json();
        })
        .then(nuevo => {
            bootstrap.Modal.getInstance(document.getElementById('modalNuevoAlquiler')).hide();
            document.getElementById('formNuevoAlquiler').reset();
            mostrarAlertaGeneral('Reserva #' + nuevo.idAlquiler + ' creada correctamente.', 'success');
            cargarAlquileres();
        })
        .catch(error => {
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
        .then(async response => {
            if (!response.ok) {
                const errorMsg = await response.text();
                throw new Error(errorMsg || 'No se pudo entregar el alquiler.');
            }
            return response.json();
        })
        .then(res => {
            mostrarAlertaGeneral(
                'Alquiler #' + idAlquiler + ' entregado (ACTIVO). Depósito cobrado: Q ' + Number(res.depositoCobrado).toFixed(2) + '.',
                'success'
            );
            cargarAlquileres();
        })
        .catch(error => {
            mostrarAlertaGeneral(error.message, 'danger');
        });
}

function alternarCamposPago() {
    const metodo = document.getElementById('devMetodoPago').value;
    document.getElementById('bloquePagoEfectivo').classList.toggle('d-none', metodo !== 'EFECTIVO');
    document.getElementById('bloquePagoTarjeta').classList.toggle('d-none', metodo !== 'TARJETA');
}

function abrirModalDevolucion(idAlquiler) {
    document.getElementById('formDevolucion').reset();
    document.getElementById('alertaDevolucion').classList.add('d-none');
    document.getElementById('devolucionIdAlquiler').value = idAlquiler;
    document.getElementById('lblDevolucionId').innerText = idAlquiler;
    alternarCamposPago();
    new bootstrap.Modal(document.getElementById('modalDevolucion')).show();
}

function procesarDevolucion(event) {
    event.preventDefault();
    const alerta = document.getElementById('alertaDevolucion');
    alerta.classList.add('d-none');

    const idAlquiler = document.getElementById('devolucionIdAlquiler').value;
    const eqDanado = parseInt(document.getElementById('devEquipoDanado').value);
    const accExtraviado = document.getElementById('devAccesorioExtraviado').value;
    const metodoPago = document.getElementById('devMetodoPago').value;

    const equiposDevueltos = [];
    if (!isNaN(eqDanado)) {
        equiposDevueltos.push({ idEquipo: eqDanado, estadoDevolucion: 'DAÑADO' });
    }

    const accesoriosDevueltos = [];
    if (accExtraviado.trim() !== '') {
        const partes = accExtraviado.split(':');
        if (partes.length === 2) {
            accesoriosDevueltos.push({
                idAccesorio: parseInt(partes[0].trim()),
                cantidad: parseInt(partes[1].trim()),
                unidadesExtraviadas: parseInt(partes[1].trim())
            });
        }
    }

    const payload = {
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
        .then(async response => {
            if (!response.ok) {
                const errorMsg = await response.text();
                throw new Error(errorMsg);
            }
            return response.json();
        })
        .then(res => {
            let resumen =
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
            bootstrap.Modal.getInstance(document.getElementById('modalDevolucion')).hide();
            document.getElementById('formDevolucion').reset();
            cargarAlquileres();
        })
        .catch(error => {
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
    const alerta = document.getElementById('alertaGeneral');
    alerta.textContent = mensaje;
    alerta.className = 'alert alert-' + tipo;
    alerta.classList.remove('d-none');
    setTimeout(() => alerta.classList.add('d-none'), 5000);
}