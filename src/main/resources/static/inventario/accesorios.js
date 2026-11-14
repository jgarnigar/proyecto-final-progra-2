let modalAccesorio;
let modalMantenimiento;

document.addEventListener("DOMContentLoaded", () => {
    modalAccesorio = new bootstrap.Modal(document.getElementById('modalAccesorio'));
    modalMantenimiento = new bootstrap.Modal(document.getElementById('modalMantenimiento'));

    cargarAccesorios();
    cargarDatosEquipos();

    document.getElementById("formAccesorio").addEventListener("submit", guardarAccesorio);
    document.getElementById("formMantenimiento").addEventListener("submit", guardarMantenimiento);
    document.getElementById("modalMantenimiento").addEventListener("show.bs.modal", cargarDatosEquipos);
});

function cargarAccesorios() {
    fetch('/api/accesorios')
        .then(res => {
            if (!res.ok) throw new Error('Error de servidor');
            return res.json();
        })
        .then(accesorios => {
            const tbody = document.getElementById("cuerpoTablaAccesorios");
            tbody.innerHTML = accesorios.length ? "" : `<tr><td colspan="6" class="text-center text-muted">No hay accesorios registrados.</td></tr>`;

            accesorios.forEach(acc => {
                const stockColor = acc.existenciaActual <= 5 ? "text-danger fw-bold" : "";

                tbody.innerHTML += `
                    <tr>
                        <td class="fw-bold">#${acc.idAccesorio}</td>
                        <td>${acc.nombre}</td>
                        <td>Q ${Number(acc.tarifaFija).toFixed(2)}</td>
                        <td>Q ${Number(acc.precioReposicion).toFixed(2)}</td>
                        <td class="${stockColor}">${acc.existenciaActual}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-primary" onclick="abrirModalEditar(${acc.idAccesorio}, '${acc.nombre}', ${acc.tarifaFija}, ${acc.precioReposicion}, ${acc.existenciaActual})">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger" onclick="desactivarAccesorio(${acc.idAccesorio})">
                                <i class="bi bi-trash"></i>
                            </button>
                        </td>
                    </tr>
                `;
            });
        })
        .catch(err => {
            console.error("Error cargando accesorios:", err);
            mostrarAlerta("No se pudieron cargar los accesorios.", "danger");
        });
}

function abrirModalCrear() {
    document.getElementById("formAccesorio").reset();
    document.getElementById("accesorioId").value = "";
    document.getElementById("tituloModal").textContent = "Registrar Nuevo Accesorio";
    document.getElementById("alertaError").classList.add("d-none");
    modalAccesorio.show();
}

function abrirModalEditar(id, nombre, tarifa, repo, existencia) {
    document.getElementById("formAccesorio").reset();
    document.getElementById("accesorioId").value = id;
    document.getElementById("nombreAcc").value = nombre;
    document.getElementById("tarifaAcc").value = tarifa;
    document.getElementById("reposicionAcc").value = repo;
    document.getElementById("stockAcc").value = existencia;
    document.getElementById("tituloModal").textContent = "Editar Accesorio #" + id;
    document.getElementById("alertaError").classList.add("d-none");
    modalAccesorio.show();
}

function guardarAccesorio(e) {
    e.preventDefault();
    const id = document.getElementById("accesorioId").value;
    const metodo = id ? 'PUT' : 'POST';
    const url = id ? `/api/accesorios/${id}` : '/api/accesorios';

    const data = {
        nombre: document.getElementById("nombreAcc").value,
        tarifaFija: parseFloat(document.getElementById("tarifaAcc").value),
        precioReposicion: parseFloat(document.getElementById("reposicionAcc").value),
        existenciaActual: parseInt(document.getElementById("stockAcc").value)
    };

    fetch(url, {
        method: metodo,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (res.ok) return res.json();
            return res.text().then(t => { throw new Error(t || "Error al guardar el accesorio"); });
        })
        .then(() => {
            modalAccesorio.hide();
            cargarAccesorios();
        })
        .catch(err => {
            const alerta = document.getElementById("alertaError");
            alerta.textContent = err.message;
            alerta.classList.remove("d-none");
        });
}

function desactivarAccesorio(id) {
    if (confirm("¿Retirar todo el stock (poner en 0) para este accesorio?")) {
        fetch(`/api/accesorios/${id}`, { method: 'DELETE' })
            .then(res => {
                if (res.ok) cargarAccesorios();
            })
            .catch(err => console.error("Error al eliminar stock:", err));
    }
}

function cargarDatosEquipos() {
    Promise.all([
        fetch("/api/equipos").then(res => res.ok ? res.json() : Promise.reject("No se pudieron consultar los equipos.")),
        fetch("/api/accesorios").then(res => res.ok ? res.json() : Promise.reject("No se pudieron consultar los accesorios."))
    ])
        .then(([equipos, accesorios]) => {
            llenarSelectorHistorial(equipos, accesorios);
            llenarSelectorMantenimiento(equipos, accesorios);
            mostrarEquiposEnMantenimiento(equipos);
        })
        .catch(error => {
            console.error("Error cargando datos:", error);
            mostrarAlertaMantenimiento(typeof error === "string" ? error : error.message, "danger");
        });
}

function llenarSelectorHistorial(equipos, accesorios) {
    const selector = document.getElementById("equipoHistorial");
    const seleccionado = selector.value;
    selector.innerHTML = `<option value="">Seleccione un equipo o accesorio...</option>`;

    const grupoEquipos = document.createElement("optgroup");
    grupoEquipos.label = "Equipos";
    equipos.forEach(equipo => {
        const opcion = document.createElement("option");
        opcion.value = `E-${equipo.idEquipo}`;
        opcion.textContent = `#${equipo.idEquipo} - ${equipo.tipo} ${equipo.modelo}`;
        grupoEquipos.appendChild(opcion);
    });
    selector.appendChild(grupoEquipos);

    const grupoAccesorios = document.createElement("optgroup");
    grupoAccesorios.label = "Accesorios";
    accesorios.forEach(acc => {
        const opcion = document.createElement("option");
        opcion.value = `A-${acc.idAccesorio}`;
        opcion.textContent = `#${acc.idAccesorio} - ${acc.nombre}`;
        grupoAccesorios.appendChild(opcion);
    });
    selector.appendChild(grupoAccesorios);

    if (seleccionado) selector.value = seleccionado;
}

function llenarSelectorMantenimiento(equipos, accesorios) {
    const selector = document.getElementById("equipoMantenimiento");
    selector.innerHTML = `<option value="">Seleccione un ítem disponible...</option>`;

    const grupoEquipos = document.createElement("optgroup");
    grupoEquipos.label = "Equipos Disponibles";
    equipos
        .filter(equipo => equipo.estado === "DISPONIBLE")
        .forEach(equipo => {
            const opcion = document.createElement("option");
            opcion.value = `E-${equipo.idEquipo}`;
            opcion.textContent = `#${equipo.idEquipo} - ${equipo.tipo} ${equipo.modelo}`;
            grupoEquipos.appendChild(opcion);
        });
    selector.appendChild(grupoEquipos);

    const grupoAccesorios = document.createElement("optgroup");
    grupoAccesorios.label = "Accesorios en Stock";
    accesorios
        .filter(acc => acc.existenciaActual > 0)
        .forEach(acc => {
            const opcion = document.createElement("option");
            opcion.value = `A-${acc.idAccesorio}`;
            opcion.textContent = `#${acc.idAccesorio} - ${acc.nombre} (Stock: ${acc.existenciaActual})`;
            grupoAccesorios.appendChild(opcion);
        });
    selector.appendChild(grupoAccesorios);
}

function mostrarEquiposEnMantenimiento(equipos) {
    const tbody = document.getElementById("cuerpoTablaMantenimiento");
    const equiposMantenimiento = equipos.filter(equipo => equipo.estado === "EN MANTENIMIENTO");

    tbody.innerHTML = "";
    if (equiposMantenimiento.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No hay equipos en mantenimiento.</td></tr>`;
        return;
    }

    equiposMantenimiento.forEach(equipo => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>#${equipo.idEquipo}</td>
            <td>${equipo.tipo}</td>
            <td>${equipo.modelo}</td>
            <td><span class="badge bg-warning text-dark">${equipo.estado}</span></td>
            <td>
                <button class="btn btn-sm btn-success" onclick="cerrarMantenimiento(${equipo.idEquipo})">
                    <i class="bi bi-check-circle"></i> Cerrar
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function cargarEquiposEnMantenimiento() {
    cargarDatosEquipos();
}

function guardarMantenimiento(evento) {
    evento.preventDefault();
    const alerta = document.getElementById("alertaErrorMantenimiento");
    alerta.classList.add("d-none");

    const seleccion = document.getElementById("equipoMantenimiento").value;
    if (!seleccion) {
        mostrarErrorMantenimiento("Debe seleccionar un equipo o accesorio.");
        return;
    }

    const [tipoItem, idTexto] = seleccion.split('-');
    const idReal = Number(idTexto);

    if (tipoItem === 'E') {
        const mantenimiento = {
            idEquipo: idReal,
            fecha: document.getElementById("fechaMantenimiento").value || null,
            motivo: document.getElementById("motivoMantenimiento").value.trim(),
            costo: Number(document.getElementById("costoMantenimiento").value)
        };
        enviarMantenimientoBackend("/api/mantenimientos", mantenimiento);
    } else if (tipoItem === 'A') {
        const historialAccesorio = {
            idAccesorio: idReal,
            fecha: document.getElementById("fechaMantenimiento").value || null,
            cantidad: 1,
            tipoMovimiento: "SALIDA_POR_MANTENIMIENTO"
        };
        enviarMantenimientoBackend("/api/mantenimientos/accesorio", historialAccesorio);
    }
}

function enviarMantenimientoBackend(url, payload) {
    fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(async response => {
            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || "No se pudo registrar el mantenimiento.");
            }
            return response.json();
        })
        .then(() => {
            modalMantenimiento.hide();
            document.getElementById("formMantenimiento").reset();
            cargarDatosEquipos();
            cargarAccesorios();
            mostrarAlertaMantenimiento("Registro guardado correctamente.", "success");
        })
        .catch(error => {
            mostrarErrorMantenimiento(error.message);
        });
}

function cargarHistorialMantenimiento() {
    const seleccion = document.getElementById("equipoHistorial").value;
    const tbody = document.getElementById("cuerpoHistorialMantenimiento");
    tbody.innerHTML = "";

    if (!seleccion) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Seleccione un equipo o accesorio para consultar su historial.</td></tr>`;
        return;
    }

    const [tipoItem, idTexto] = seleccion.split('-');
    const idReal = Number(idTexto);

    const url = (tipoItem === 'E')
        ? `/api/mantenimientos/equipo/${idReal}`
        : `/api/mantenimientos/accesorio/${idReal}`;

    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error("No se pudo consultar el historial.");
            return response.json();
        })
        .then(historial => {
            if (historial.length === 0) {
                tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Este ítem no tiene registros en su historial.</td></tr>`;
                return;
            }

            historial.forEach(item => {
                const tr = document.createElement("tr");
                const fechaFormat = item.fecha ? new Date(item.fecha).toLocaleString() : "Sin fecha";

                if (tipoItem === 'E') {
                    tr.innerHTML = `
                        <td>${item.idMantenimiento}</td>
                        <td>${fechaFormat}</td>
                        <td>${item.motivo}</td>
                        <td>Q ${Number(item.costo).toFixed(2)}</td>
                    `;
                } else {
                    tr.innerHTML = `
                        <td>${item.idMovimiento}</td>
                        <td>${fechaFormat}</td>
                        <td>${item.tipoMovimiento}</td>
                        <td>Cant: ${item.cantidad}</td>
                    `;
                }
                tbody.appendChild(tr);
            });
        })
        .catch(error => {
            console.error(error);
            mostrarAlertaMantenimiento(error.message, "danger");
        });
}

function cerrarMantenimiento(idEquipo) {
    const confirmar = confirm(`¿Desea cerrar el mantenimiento del equipo #${idEquipo}?`);
    if (!confirmar) return;

    fetch(`/api/mantenimientos/cerrar/${idEquipo}`, { method: "PUT" })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || "No se pudo cerrar el mantenimiento.");
                });
            }
            return response.text();
        })
        .then(() => {
            mostrarAlertaMantenimiento("Mantenimiento cerrado. El equipo está disponible.", "success");
            cargarDatosEquipos();
        })
        .catch(error => {
            mostrarAlertaMantenimiento(error.message, "danger");
        });
}

function mostrarAlerta(mensaje, tipo) {
    const alerta = document.getElementById("alertaGeneral");
    alerta.textContent = mensaje;
    alerta.className = `alert alert-${tipo}`;
    alerta.classList.remove("d-none");

    setTimeout(() => alerta.classList.add("d-none"), 4000);
}

function mostrarAlertaMantenimiento(mensaje, tipo) {
    const alerta = document.getElementById("alertaMantenimiento");
    alerta.textContent = mensaje;
    alerta.className = `alert alert-${tipo}`;
    alerta.classList.remove("d-none");

    setTimeout(() => alerta.classList.add("d-none"), 4000);
}

function mostrarErrorMantenimiento(mensaje) {
    const alerta = document.getElementById("alertaErrorMantenimiento");
    alerta.textContent = mensaje;
    alerta.classList.remove("d-none");
}