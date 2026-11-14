const API_URL = '/api/alquileres';

document.addEventListener('DOMContentLoaded', cargarAlquileres);

function cargarAlquileres() {
    fetch(API_URL)
        .then(response => {
            if (!response.ok) throw new Error('Error al conectar con la API: ' + response.status);
            return response.json();
        })
        .then(alquileres => {
            console.log(alquileres);

            const cuerpoTabla = document.getElementById('cuerpoTablaAlquileres');
            cuerpoTabla.innerHTML = '';

            if (!alquileres || alquileres.length === 0) {
                cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No hay alquileres registrados aún.</td></tr>`;
                return;
            }

            alquileres.forEach(alq => {
                const id = alq.idAlquiler || alq.id || 'N/A';
                const clienteInfo = alq.cliente ? alq.cliente.nombre : (alq.idCliente || 'N/A');
                const deposito = alq.depositoCobrado !== undefined ? alq.depositoCobrado : 0;
                const estado = alq.estado || 'DESCONOCIDO';
                const fecha = alq.fechaEstimada ? new Date(alq.fechaEstimada).toLocaleString() : 'Sin fecha';

                let badgeClass = 'bg-secondary';
                if (estado === 'RESERVADO') badgeClass = 'bg-warning text-dark';
                if (estado === 'ACTIVO') badgeClass = 'bg-primary';
                if (estado === 'FINALIZADO') badgeClass = 'bg-success';

                let botones = '';
                if (estado === 'RESERVADO') {
                    botones = `<button class="btn btn-sm btn-success me-1" onclick="entregarAlquiler(${id})">Entregar</button>`;
                } else if (estado === 'ACTIVO') {
                    botones = `<button class="btn btn-sm btn-dark" onclick="abrirModalDevolucion(${id})">Recibir / Cobrar</button>`;
                }

                const fila = document.createElement('tr');
                fila.innerHTML = `
                    <td class="fw-bold">#${id}</td>
                    <td>${clienteInfo}</td>
                    <td>Q ${parseFloat(deposito).toFixed(2)}</td>
                    <td>${fecha}</td>
                    <td><span class="badge ${badgeClass}">${estado}</span></td>
                    <td>${botones}</td>
                `;
                cuerpoTabla.appendChild(fila);
            });
        })
        .catch(error => {
            console.error('Error cargando la tabla:', error);
            const cuerpoTabla = document.getElementById('cuerpoTablaAlquileres');
            cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-danger">Ocurrió un error al cargar los datos: ${error.message}</td></tr>`;
        });
}

function registrarReserva(event) {
    event.preventDefault();

    const idCliente = document.getElementById('reservaIdCliente').value;
    const fechaEstimada = document.getElementById('reservaFechaEstimada').value;

    const equiposInput = document.getElementById('reservaEquipos').value;
    const equiposLista = equiposInput.split(',').map(id => ({
        idEquipo: parseInt(id.trim()),
        estadoDevolucion: "BUENO"
    })).filter(eq => !isNaN(eq.idEquipo));

    const accInput = document.getElementById('reservaAccesorios').value;
    const accesoriosLista = accInput ? accInput.split(',').map(item => {
        const partes = item.split(':');
        return {
            idAccesorio: parseInt(partes[0].trim()),
            cantidad: parseInt(partes[1].trim()),
            unidadesExtraviadas: 0
        };
    }).filter(acc => !isNaN(acc.idAccesorio)) : [];

    const payload = {
        idCliente: parseInt(idCliente),
        fechaEstimada: fechaEstimada,
        alquilerEquiposLista: equiposLista,
        alquilerAccesoriosLista: accesoriosLista
    };

    fetch(API_URL, {
        method: 'POST',
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
        .then(data => {
            alert('Reserva creada con éxito. ID: ' + (data.idAlquiler || data.id));
            bootstrap.Modal.getInstance(document.getElementById('modalNuevoAlquiler')).hide();
            document.getElementById('formNuevaReserva').reset();
            cargarAlquileres();
        })
        .catch(error => alert('Error al crear reserva: ' + error.message));
}

function entregarAlquiler(idAlquiler) {
    if (!confirm(`¿Confirmas la entrega de los equipos para el alquiler #${idAlquiler}? Se cobrará el depósito.`)) return;

    fetch(`${API_URL}/${idAlquiler}/entregar`, {
        method: 'PUT'
    })
        .then(async response => {
            if (!response.ok) {
                const errorMsg = await response.text();
                throw new Error(errorMsg);
            }
            return response.json();
        })
        .then(() => {
            alert('Equipos entregados y alquiler activado.');
            cargarAlquileres();
        })
        .catch(error => alert('Error al entregar: ' + error.message));
}

function abrirModalDevolucion(idAlquiler) {
    document.getElementById('devolucionIdAlquiler').value = idAlquiler;
    document.getElementById('lblDevolucionId').innerText = idAlquiler;
    new bootstrap.Modal(document.getElementById('modalDevolucion')).show();
}

function procesarDevolucion(event) {
    event.preventDefault();

    const idAlquiler = document.getElementById('devolucionIdAlquiler').value;
    const eqDanado = parseInt(document.getElementById('devEquipoDanado').value);
    const accExtraviado = document.getElementById('devAccesorioExtraviado').value;

    const equiposDevueltos = [];
    if (!isNaN(eqDanado)) {
        equiposDevueltos.push({ idEquipo: eqDanado, estadoDevolucion: "DAÑADO" });
    }

    const accesoriosDevueltos = [];
    if (accExtraviado) {
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
        alquilerAccesoriosLista: accesoriosDevueltos
    };

    fetch(`${API_URL}/${idAlquiler}/devolver`, {
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
        .then(() => {
            alert('Alquiler finalizado correctamente.');
            bootstrap.Modal.getInstance(document.getElementById('modalDevolucion')).hide();
            document.getElementById('formDevolucion').reset();
            cargarAlquileres();
        })
        .catch(error => alert('Error en devolución: ' + error.message));
}