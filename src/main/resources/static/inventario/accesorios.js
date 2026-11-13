// accesorios.js
let modalAccesorio;
let modalMantenimiento;

document.addEventListener("DOMContentLoaded", () => {
    // Inicializar el modal de Bootstrap
    modalAccesorio = new bootstrap.Modal(document.getElementById('modalAccesorio'));
    modalMantenimiento = new bootstrap.Modal(document.getElementById('modalMantenimiento'));

    cargarAccesorios();
    cargarDatosEquipos();

    // Interceptar el formulario para crear o editar
    document.getElementById("formAccesorio").addEventListener("submit", guardarAccesorio);
    document.getElementById("formMantenimiento").addEventListener("submit", guardarMantenimiento);
    document.getElementById("modalMantenimiento").addEventListener("show.bs.modal", cargarDatosEquipos);
});

//Accesorios
function cargarAccesorios() {
    fetch('/api/accesorios')
     .then(response => {
            if (!response.ok) throw new Error('Error al conectar con el servidor');
            return response.json();
        })
        .then(accesorios => {
            const tbody = document.getElementById("cuerpoTablaAccesorios");
            tbody.innerHTML = "";

            if (accesorios.length === 0) {
                tbody.innerHTML = 
                `<tr>
                    <td colspan="6" class="text-center text-muted"> No hay accesorios registrados.
                </td>
            </tr>`;
                return;
            }

            accesorios.forEach(accesorio => {
                const tr = document.createElement("tr");

                tr.innerHTML = `
                    <td class="fw-bold">#${accesorio.idAccesorio}</td>
                    <td>${accesorio.nombre}</td>
                    <td>Q ${Number(accesorio.tarifaFija).toFixed(2)}</td>
                    <td>Q ${Number(accesorio.precioReposicion).toFixed(2)}</td>
                    <td>${accesorio.existenciaActual}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary"
                            onclick="abrirModalEditar(${accesorio.idAccesorio})">
                            <i class="bi bi-pencil"></i>
                        </button>
                    </td>
                `;

                tbody.appendChild(tr);
            });
        })
        .catch(error => {
            console.error("Error cargando accesorios:", error);
            mostrarAlerta("No se pudieron cargar los accesorios.", "danger");
        });
}

// Preparar formulario para un NUEVO accesorio
function abrirModalCrear() {
    document.getElementById("formAccesorio").reset();
    document.getElementById("accesorioId").value = "";
    document.getElementById("tituloModal").textContent = "Registrar Nuevo Accesorio";
    document.getElementById("alertaError").classList.add("d-none");

    modalAccesorio.show();
}

// Preparar formulario para EDITAR un accesorio existente
function abrirModalEditar(id) {
    fetch(`/api/accesorios/${id}`)
        .then(response => {
            if (!response.ok) throw new Error("No se pudo consultar el accesorio");
            return response.json();
        })
        .then(accesorio => {
            document.getElementById("formAccesorio").reset();

            document.getElementById("accesorioId").value = accesorio.idAccesorio;
            document.getElementById("nombreAccesorio").value = accesorio.nombre;
            document.getElementById("tarifaFija").value = accesorio.tarifaFija;
            document.getElementById("precioReposicion").value = accesorio.precioReposicion;
            document.getElementById("existenciaActual").value = accesorio.existenciaActual;

            document.getElementById("tituloModal").textContent = "Editar Accesorio #" + id;
            document.getElementById("alertaError").classList.add("d-none");

            modalAccesorio.show();
        })
        .catch(error => {
            alert(error.message);
        });
}

// Guardar un accesorio nuevo o modificar uno existente
function guardarAccesorio(evento) {
    evento.preventDefault();

    const alertaError = document.getElementById("alertaError");
    alertaError.classList.add("d-none");

    const id = document.getElementById("accesorioId").value;

    const url = id ? `/api/accesorios/${id}` : '/api/accesorios';
    const method = id ? 'PUT' : 'POST';

    const accesorioData = {
        nombre: document.getElementById("nombreAccesorio").value,
        tarifaFija: Number(document.getElementById("tarifaFija").value),
        precioReposicion: Number(document.getElementById("precioReposicion").value),
        existenciaActual: Number(document.getElementById("existenciaActual").value)
    };

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(accesorioData)
    })
        .then(response => {
            if (response.ok) return response.json();

            return response.text().then(text => {
                throw new Error(text || "No se pudo guardar el accesorio");
            });
        })
        .then(() => {
            modalAccesorio.hide();
            cargarAccesorios();
        })
        .catch(error => {
            alertaError.textContent = error.message;
            alertaError.classList.remove("d-none");
        });
}
//Mantenimiento
function cargarDatosEquipos() {
    fetch("/api/equipos")
        .then(response => {
            if (!response.ok) {
                throw new Error("No se pudieron consultar los equipos.");
            }
            return response.json();
        })
        .then(equipos => {
            llenarSelectorHistorial(equipos);
            llenarSelectorMantenimiento(equipos);
            mostrarEquiposEnMantenimiento(equipos);
        })
        .catch(error => {
            console.error("Error cargando equipos:", error);
            mostrarAlertaMantenimiento(error.message, "danger");
        });
}

// Llenar selector con todos los equipos para consultar historial
function llenarSelectorHistorial(equipos) {
    const selector = document.getElementById("equipoHistorial");
    const seleccionado = selector.value;

    selector.innerHTML = ` <option value="">Seleccione un equipo</option> `;

    equipos.forEach(equipo => {
        const opcion = document.createElement("option");
        opcion.value = equipo.idEquipo;
        opcion.textContent =
            `#${equipo.idEquipo} - ${equipo.tipo} ${equipo.modelo}`;

        selector.appendChild(opcion);
    });

    if (seleccionado) {
        selector.value = seleccionado;
    }
}

// Llenar selector únicamente con equipos disponibles
function llenarSelectorMantenimiento(equipos) {
    const selector = document.getElementById("equipoMantenimiento");

    selector.innerHTML = ` <option value="">Seleccione un equipo disponible</option> `;

    equipos
        .filter(equipo => equipo.estado === "DISPONIBLE")
        .forEach(equipo => {
            const opcion = document.createElement("option");
            opcion.value = equipo.idEquipo;
            opcion.textContent =
                `#${equipo.idEquipo} - ${equipo.tipo} ${equipo.modelo}`;

            selector.appendChild(opcion);
        });
}

// Mostrar equipos que actualmente están en mantenimiento
function mostrarEquiposEnMantenimiento(equipos) {
    const tbody = document.getElementById("cuerpoTablaMantenimiento");

    const equiposMantenimiento = equipos.filter(
        equipo => equipo.estado === "EN MANTENIMIENTO"
    );

    tbody.innerHTML = "";

    if (equiposMantenimiento.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center text-muted">
                    No hay equipos en mantenimiento.
                </td>
            </tr>`;
        return;
    }

    equiposMantenimiento.forEach(equipo => {
        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>#${equipo.idEquipo}</td>
            <td>${equipo.tipo}</td>
            <td>${equipo.modelo}</td>
            <td>
                <span class="badge bg-warning text-dark">
                    ${equipo.estado}
                </span>
            </td>
            <td>
                <button class="btn btn-sm btn-success"
                    onclick="cerrarMantenimiento(${equipo.idEquipo})">
                    <i class="bi bi-check-circle"></i>
                    Cerrar
                </button>
            </td>
        `;

        tbody.appendChild(tr);
    });
}

// Cargar nuevamente la información al entrar a la pestaña
function cargarEquiposEnMantenimiento() {
    cargarDatosEquipos();
}

// Registrar un mantenimiento
function guardarMantenimiento(evento) {
    evento.preventDefault();

    const alerta = document.getElementById("alertaErrorMantenimiento");
    alerta.classList.add("d-none");

    const mantenimiento = {
        idEquipo: Number(document.getElementById("equipoMantenimiento").value),
        fecha: document.getElementById("fechaMantenimiento").value || null,
        motivo: document.getElementById("motivoMantenimiento").value.trim(),
        costo: Number(document.getElementById("costoMantenimiento").value)
    };

    if (!mantenimiento.idEquipo) {
        mostrarErrorMantenimiento("Debe seleccionar un equipo.");
        return;
    }

    fetch("/api/mantenimientos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(mantenimiento)
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || "No se pudo registrar el mantenimiento.");
                });
            }
            return response.json();
        })
        .then(() => {
            modalMantenimiento.hide();
            document.getElementById("formMantenimiento").reset();

            cargarDatosEquipos();

            mostrarAlertaMantenimiento( "Mantenimiento registrado correctamente.",
                "success" );
        })
        .catch(error => {
            mostrarErrorMantenimiento(error.message);
        });
}

// Consultar historial de mantenimiento de un equipo
function cargarHistorialMantenimiento() {
    const idEquipo = document.getElementById("equipoHistorial").value;
    const tbody = document.getElementById("cuerpoHistorialMantenimiento");

    tbody.innerHTML = "";

    if (!idEquipo) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center text-muted">
                    Seleccione un equipo para consultar su historial.
                </td>
            </tr>`;
        return;
    }

    fetch(`/api/mantenimientos/equipo/${idEquipo}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("No se pudo consultar el historial.");
            }
            return response.json();
        })
        .then(historial => {
            if (historial.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="4" class="text-center text-muted">
                            Este equipo no tiene mantenimientos registrados.
                        </td>
                    </tr>`;
                return;
            }

            historial.forEach(mantenimiento => {
                const tr = document.createElement("tr");

                tr.innerHTML = `
                    <td>${mantenimiento.idMantenimiento}</td>
                    <td>${mantenimiento.fecha
                        ? new Date(mantenimiento.fecha).toLocaleString()
                        : "Sin fecha"}</td>
                    <td>${mantenimiento.motivo}</td>
                    <td>Q ${Number(mantenimiento.costo).toFixed(2)}</td>
                `;

                tbody.appendChild(tr);
            });
        })
        .catch(error => {
            console.error(error);
            mostrarAlertaMantenimiento(error.message, "danger");
        });
}

// Cerrar mantenimiento y devolver el equipo a estado DISPONIBLE
function cerrarMantenimiento(idEquipo) {
    const confirmar = confirm(
        `¿Desea cerrar el mantenimiento del equipo #${idEquipo}?`
    );

    if (!confirmar) return;

    fetch(`/api/mantenimientos/cerrar/${idEquipo}`, {
        method: "PUT"
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || "No se pudo cerrar el mantenimiento.");
                });
            }
            return response.text();
        })
        .then(() => {
            mostrarAlertaMantenimiento(
                "Mantenimiento cerrado. El equipo está disponible.",
                "success"
            );

            cargarDatosEquipos();
        })
        .catch(error => {
            mostrarAlertaMantenimiento(error.message, "danger");
        });
}


// Mostrar mensajes
function mostrarAlerta(mensaje, tipo) {
    const alerta = document.getElementById("alertaGeneral");
    alerta.textContent = mensaje;
    alerta.className = `alert alert-${tipo}`;
}
function mostrarAlertaMantenimiento(mensaje, tipo) {
    const alerta = document.getElementById("alertaMantenimiento");
    alerta.textContent = mensaje;
    alerta.className = `alert alert-${tipo}`;
}

function mostrarErrorMantenimiento(mensaje) {
    const alerta = document.getElementById("alertaErrorMantenimiento");
    alerta.textContent = mensaje;
    alerta.classList.remove("d-none");
}
