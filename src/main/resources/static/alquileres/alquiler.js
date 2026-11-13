// URL base de tu API Spring Boot
const API_URL = '/api/alquileres';

// Cargar la tabla al iniciar
document.addEventListener('DOMContentLoaded', cargarAlquileres);

// 1. GET: Cargar todos los alquileres
function cargarAlquileres() {
    fetch(API_URL)
        .then(response => response.json())
        .then(alquileres => {
            const cuerpoTabla = document.getElementById('cuerpoTablaAlquileres');
            cuerpoTabla.innerHTML = '';

            alquileres.forEach(alq => {
                const fila = document.createElement('tr');

                // Colores para los estados
                let badgeClass = 'bg-secondary';
                if (alq.estado === 'RESERVADO') badgeClass = 'bg-warning text-dark';
                if (alq.estado === 'ACTIVO') badgeClass = 'bg-primary';
                if (alq.estado === 'FINALIZADO') badgeClass = 'bg-success';

                // Botones dinámicos según el estado
                let botones = '';
                if (alq.estado === 'RESERVADO') {
                    botones = `<button class="btn btn-sm btn-success me-1" onclick="entregarAlquiler(${alq.idAlquiler})">Entregar</button>`;
                } else if (alq.estado === 'ACTIVO') {
                    botones = `<button class="btn btn-sm btn-dark" onclick="abrirModalDevolucion(${alq.idAlquiler})">Recibir / Cobrar</button>`;
                }

                fila.innerHTML = `
                    <td class="fw-bold">#${alq.idAlquiler}</td>
                    <td>${alq.idCliente}</td>
                    <td>Q ${alq.depositoCobrado.toFixed(2)}</td>
                    <td>${new Date(alq.fechaEstimada).toLocaleString()}</td>
                    <td><span class="badge ${badgeClass}">${alq.estado}</span></td>
                    <td>${botones}</td>
                `;
                cuerpoTabla.appendChild(fila);
            });
        })
        .catch(error => console.error('Error al cargar alquileres:', error));
}

// 2. POST: Registrar nueva reserva
function registrarReserva(event) {
    event.preventDefault(); // Evita que se recargue la página

    const idCliente = document.getElementById('reservaIdCliente').value;
    const fechaEstimada = document.getElementById('reservaFechaEstimada').value;

    // Convertir el input de equipos "1, 2" a un arreglo de objetos JSON
    const equiposInput = document.getElementById('reservaEquipos').value;
    const equiposLista = equiposInput.split(',').map(id => ({
        idEquipo: parseInt(id.trim()),
        estadoDevolucion: "BUENO"
    })).filter(eq => !isNaN(eq.idEquipo));

    // Convertir el input de accesorios "1:2, 2:1" a objetos JSON
    const accInput = document.getElementById('reservaAccesorios').value;
    const accesoriosLista = accInput ? accInput.split(',').map(item => {
        const partes = item.split(':');
        return {
            idAccesorio: parseInt(partes[0].trim()),
            cantidad: parseInt(partes[1].trim()),
            unidadesExtraviadas: 0
        };
    }).filter(acc => !isNaN(acc.idAccesorio)) : [];

    // Construir el JSON exacto que espera Spring Boot
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
            alert('Reserva creada con éxito. ID: ' + data.idAlquiler);
            bootstrap.Modal.getInstance(document.getElementById('modalNuevoAlquiler')).hide();
            document.getElementById('formNuevaReserva').reset();
            cargarAlquileres();
        })
        .catch(error => alert('Error al crear reserva: ' + error.message));
}

// 3. PUT: Entregar Alquiler (RESERVADO -> ACTIVO)
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
        .then(data => {
            alert('Equipos entregados y alquiler activado.');
            cargarAlquileres();
        })
        .catch(error => alert('Error al entregar: ' + error.message));
}

// 4. PUT: Preparar y enviar Devolución (ACTIVO -> FINALIZADO)
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

    // Construir listas de devolución (simulando los datos de lo dañado)
    const equiposDevueltos = [];
    if (!isNaN(eqDanado)) {
        equiposDevueltos.push({ idEquipo: eqDanado, estadoDevolucion: "DAÑADO" });
    }

    const accesoriosDevueltos = [];
    if (accExtraviado) {
        const partes = accExtraviado.split(':');
        accesoriosDevueltos.push({
            idAccesorio: parseInt(partes[0].trim()),
            cantidad: parseInt(partes[1].trim()), // Asumimos que alquiló esa cantidad
            unidadesExtraviadas: parseInt(partes[1].trim())
        });
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
        .then(data => {
            alert('Alquiler finalizado correctamente.');
            bootstrap.Modal.getInstance(document.getElementById('modalDevolucion')).hide();
            document.getElementById('formDevolucion').reset();
            cargarAlquileres();
        })
        .catch(error => alert('Error en devolución: ' + error.message));
}