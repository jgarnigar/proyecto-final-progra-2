const API_CLIENTES = '/api/clientes';

var clientesCache = [];

document.addEventListener('DOMContentLoaded', function () {
    cargarClientes();
});

function cargarClientes() {
    fetch(API_CLIENTES)
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (msg) {
                    throw new Error(msg || 'No se pudieron cargar los clientes.');
                });
            }
            return response.json();
        })
        .then(function (clientes) {
            clientesCache = Array.isArray(clientes) ? clientes : [];
            renderizarTablaClientes(clientesCache);
        })
        .catch(function (error) {
            console.error('Error cargando clientes:', error);
            var tbody = document.getElementById('cuerpoTablaClientes');
            if (tbody) {
                tbody.innerHTML =
                    '<tr>' +
                    '<td colspan="6" class="text-center text-danger py-4">' +
                    'Error de conexión con el servidor: ' + error.message +
                    '</td>' +
                    '</tr>';
            }
        });
}

function renderizarTablaClientes(clientes) {
    var tbody = document.getElementById('cuerpoTablaClientes');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (clientes.length === 0) {
        tbody.innerHTML =
            '<tr>' +
            '<td colspan="6" class="text-center text-muted py-4">' +
            'No hay clientes registrados. Presiona "Nuevo Cliente" para agregar uno.' +
            '</td>' +
            '</tr>';
        return;
    }

    clientes.forEach(function (c) {
        var saldo = Number(c.saldoPendiente || 0);
        var saldoHtml = saldo > 0
            ? '<span class="text-danger fw-bold">Q ' + saldo.toFixed(2) + '</span>'
            : '<span class="text-muted">Q 0.00</span>';

        var badgeEstado = c.estado === 'ACTIVO'
            ? '<span class="badge bg-success">ACTIVO</span>'
            : '<span class="badge bg-secondary">' + c.estado + '</span>';

        var botonAbono = saldo > 0
            ? '<button type="button" class="btn btn-sm btn-outline-success me-1" onclick="abrirModalAbono(' + c.idCliente + ')">' +
            '<i class="bi bi-cash-stack me-1"></i> Abonar' +
            '</button>'
            : '';

        var acciones =
            botonAbono +
            '<button type="button" class="btn btn-sm btn-outline-primary" onclick="abrirModalEditarCliente(' + c.idCliente + ')">' +
            '<i class="bi bi-pencil-square me-1"></i> Editar' +
            '</button>';

        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td class="fw-bold">#' + c.idCliente + '</td>' +
            '<td>' + c.nombre + '</td>' +
            '<td>' + c.email + '</td>' +
            '<td>' + saldoHtml + '</td>' +
            '<td>' + badgeEstado + '</td>' +
            '<td>' + acciones + '</td>';
        tbody.appendChild(tr);
    });
}

function abrirModalNuevoCliente() {
    document.getElementById('formCliente').reset();
    document.getElementById('alertaModalCliente').classList.add('d-none');
    document.getElementById('clienteId').value = '';
    document.getElementById('clienteSaldo').value = '0.00';
    document.getElementById('clienteEstado').value = 'ACTIVO';
    document.getElementById('tituloModalCliente').innerText = 'Registrar Cliente';

    var modalEl = document.getElementById('modalCliente');
    bootstrap.Modal.getOrCreateInstance(modalEl).show();
}

function abrirModalEditarCliente(idCliente) {
    var cliente = clientesCache.find(function (c) { return c.idCliente === idCliente; });
    if (!cliente) return;

    document.getElementById('alertaModalCliente').classList.add('d-none');
    document.getElementById('clienteId').value = cliente.idCliente;
    document.getElementById('clienteNombre').value = cliente.nombre;
    document.getElementById('clienteEmail').value = cliente.email;
    document.getElementById('clienteSaldo').value = Number(cliente.saldoPendiente || 0).toFixed(2);
    document.getElementById('clienteEstado').value = cliente.estado;
    document.getElementById('tituloModalCliente').innerText = 'Editar Cliente #' + cliente.idCliente;

    var modalEl = document.getElementById('modalCliente');
    bootstrap.Modal.getOrCreateInstance(modalEl).show();
}

function guardarCliente(event) {
    event.preventDefault();
    var alerta = document.getElementById('alertaModalCliente');
    alerta.classList.add('d-none');

    var id = document.getElementById('clienteId').value;
    var esEdicion = id !== '';

    var payload = {
        nombre: document.getElementById('clienteNombre').value.trim(),
        email: document.getElementById('clienteEmail').value.trim(),
        saldoPendiente: parseFloat(document.getElementById('clienteSaldo').value || '0'),
        estado: document.getElementById('clienteEstado').value
    };

    if (esEdicion) {
        payload.idCliente = parseInt(id);
    }

    var url = esEdicion ? (API_CLIENTES + '/' + id) : API_CLIENTES;
    var metodo = esEdicion ? 'PUT' : 'POST';

    fetch(url, {
        method: metodo,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (errorMsg) {
                    throw new Error(errorMsg || 'No se pudo guardar el cliente.');
                });
            }
            return response.json();
        })
        .then(function () {
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalCliente')).hide();
            document.getElementById('formCliente').reset();
            mostrarAlertaGeneral(
                esEdicion ? 'Cliente actualizado correctamente.' : 'Cliente registrado correctamente.',
                'success'
            );
            cargarClientes();
        })
        .catch(function (error) {
            alerta.textContent = error.message;
            alerta.classList.remove('d-none');
        });
}

function abrirModalAbono(idCliente) {
    var cliente = clientesCache.find(function (c) { return c.idCliente === idCliente; });
    if (!cliente) return;

    var saldo = Number(cliente.saldoPendiente || 0);
    document.getElementById('formAbono').reset();
    document.getElementById('alertaModalAbono').classList.add('d-none');
    document.getElementById('abonoClienteId').value = cliente.idCliente;
    document.getElementById('lblAbonoCliente').innerText = '#' + cliente.idCliente + ' - ' + cliente.nombre;
    document.getElementById('lblAbonoSaldoActual').innerText = saldo.toFixed(2);

    var inputMonto = document.getElementById('abonoMonto');
    inputMonto.max = saldo.toFixed(2);
    inputMonto.value = saldo.toFixed(2);

    var modalEl = document.getElementById('modalAbono');
    bootstrap.Modal.getOrCreateInstance(modalEl).show();
}

function procesarAbono(event) {
    event.preventDefault();
    var alerta = document.getElementById('alertaModalAbono');
    alerta.classList.add('d-none');

    var idCliente = document.getElementById('abonoClienteId').value;
    var monto = parseFloat(document.getElementById('abonoMonto').value || '0');

    fetch(API_CLIENTES + '/' + idCliente + '/abonar', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ monto: monto })
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (errorMsg) {
                    throw new Error(errorMsg || 'No se pudo procesar el abono.');
                });
            }
            return response.json();
        })
        .then(function (res) {
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalAbono')).hide();
            mostrarAlertaGeneral(
                'Abono registrado. Nuevo saldo pendiente: Q ' + Number(res.saldoPendiente || 0).toFixed(2) + '.',
                'success'
            );
            cargarClientes();
        })
        .catch(function (error) {
            alerta.textContent = error.message;
            alerta.classList.remove('d-none');
        });
}

function mostrarAlertaGeneral(mensaje, tipo) {
    var alerta = document.getElementById('alertaGeneral');
    if (!alerta) return;
    alerta.textContent = mensaje;
    alerta.className = 'alert alert-' + tipo;
    alerta.classList.remove('d-none');
    setTimeout(function () { alerta.classList.add('d-none'); }, 5000);
}