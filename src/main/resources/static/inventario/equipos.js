// equipos.js

// 1. Ejecutar al cargar la página
document.addEventListener("DOMContentLoaded", () => {
    cargarEquipos();

    // Interceptar el envío del formulario
    const form = document.getElementById("formNuevoEquipo");
    form.addEventListener("submit", registrarEquipo);
});

// 2. Función para obtener los equipos mediante GET
function cargarEquipos() {
    fetch('/api/equipos')
        .then(response => {
            if (!response.ok) throw new Error('Error al conectar con el servidor');
            return response.json();
        })
        .then(equipos => {
            const tbody = document.getElementById("cuerpoTablaEquipos");
            tbody.innerHTML = ""; // Limpiar tabla

            if (equipos.length === 0) {
                tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No hay equipos registrados.</td></tr>`;
                return;
            }

            equipos.forEach(equipo => {
                const tr = document.createElement("tr");

                // Asignar color al estado usando las clases de nuestro CSS
                let badgeClass = "bg-secondary";
                if (equipo.estado === "DISPONIBLE") badgeClass = "badge-disponible";
                if (equipo.estado === "EN_CURSO" || equipo.estado === "ALQUILADO") badgeClass = "badge-alquilado";
                if (equipo.estado === "MANTENIMIENTO") badgeClass = "badge-mantenimiento";
                if (equipo.estado === "INACTIVO") badgeClass = "badge-inactivo";

                tr.innerHTML = `
                    <td class="fw-bold">#${equipo.idEquipo}</td>
                    <td>${equipo.tipo}</td>
                    <td>${equipo.modelo}</td>
                    <td>Q ${equipo.tarifaHora.toFixed(2)}</td>
                    <td>Q ${equipo.precioReposicion.toFixed(2)}</td>
                    <td>
                        <div class="progress" style="height: 20px;">
                            <div class="progress-bar ${equipo.bateriaPorcentaje < 20 ? 'bg-danger' : 'bg-success'}" 
                                 role="progressbar" style="width: ${equipo.bateriaPorcentaje}%">
                                ${equipo.bateriaPorcentaje}%
                            </div>
                        </div>
                    </td>
                    <td><span class="badge ${badgeClass}">${equipo.estado}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-danger" onclick="desactivarEquipo(${equipo.idEquipo})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(error => console.error("Error cargando equipos:", error));
}

// 3. Función para enviar los datos mediante POST
function registrarEquipo(evento) {
    evento.preventDefault(); // Evitar que la página se recargue

    const alertaError = document.getElementById("alertaError");
    alertaError.classList.add("d-none"); // Ocultar errores previos

    // Construir el objeto con los nombres exactos de la clase Java 'Equipos'
    const nuevoEquipo = {
        tipo: document.getElementById("tipoEquipo").value,
        modelo: document.getElementById("modeloEquipo").value,
        tarifaHora: parseFloat(document.getElementById("tarifaEquipo").value),
        precioReposicion: parseFloat(document.getElementById("reposicionEquipo").value),
        bateriaPorcentaje: parseInt(document.getElementById("bateriaEquipo").value)
    };

    fetch('/api/equipos', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(nuevoEquipo)
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.text().then(text => { throw new Error(text) });
            }
        })
        .then(() => {
            // Éxito: Cerrar el modal, limpiar el formulario y recargar la tabla
            const modalEl = document.getElementById('modalNuevoEquipo');
            const modalInstance = bootstrap.Modal.getInstance(modalEl);
            modalInstance.hide();

            document.getElementById("formNuevoEquipo").reset();
            cargarEquipos();
        })
        .catch(error => {
            // Mostrar el error que viene desde EquiposService en la alerta roja
            alertaError.textContent = error.message;
            alertaError.classList.remove("d-none");
        });
}

// 4. (Opcional) Función para desactivar que se conecta con el DELETE de tu compañera
function desactivarEquipo(id) {
    if(confirm("¿Estás seguro de que deseas dar de baja este equipo?")) {
        fetch(`/api/equipos/${id}`, { method: 'DELETE' })
            .then(res => {
                if(res.ok) cargarEquipos();
                else alert("No se pudo desactivar el equipo");
            });
    }
}