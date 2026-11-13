// clientes.js
let modalCliente;

document.addEventListener("DOMContentLoaded", () => {
    // Inicializar el modal de Bootstrap para poder abrirlo/cerrarlo por código
    modalCliente = new bootstrap.Modal(document.getElementById('modalCliente'));

    cargarClientes();

    // Interceptar el formulario para crear o editar
    document.getElementById("formCliente").addEventListener("submit", guardarCliente);
});

function cargarClientes() {
    fetch('/api/clientes')
        .then(response => {
            if (!response.ok) throw new Error('Error al conectar con el servidor');
            return response.json();
        })
        .then(clientes => {
            const tbody = document.getElementById("cuerpoTablaClientes");
            tbody.innerHTML = "";

            if (clientes.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No hay clientes registrados.</td></tr>`;
                return;
            }

            clientes.forEach(cliente => {
                const tr = document.createElement("tr");

                // Determinar el color del estado
                const badgeClass = cliente.estado === "ACTIVO" ? "badge-activo" : "badge-inactivo";

                // Si el saldo es mayor a 0, se pinta en rojo para alertar sobre la deuda
                const saldoClass = cliente.saldoPendiente > 0 ? "texto-deuda" : "texto-aldia";

                tr.innerHTML = `
                    <td class="fw-bold">#${cliente.idCliente}</td>
                    <td>${cliente.nombre}</td>
                    <td>${cliente.email}</td>
                    <td class="${saldoClass}">Q ${cliente.saldoPendiente.toFixed(2)}</td>
                    <td><span class="badge ${badgeClass}">${cliente.estado}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary me-1" onclick="abrirModalEditar(${cliente.idCliente}, '${cliente.nombre}', '${cliente.email}')">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="desactivarCliente(${cliente.idCliente})">
                            <i class="bi bi-person-x"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(error => console.error("Error cargando clientes:", error));
}

// Prepara el formulario para un NUEVO registro
function abrirModalCrear() {
    document.getElementById("formCliente").reset();
    document.getElementById("clienteId").value = ""; // ID vacío indica que es una creación
    document.getElementById("tituloModal").textContent = "Registrar Nuevo Cliente";
    document.getElementById("alertaError").classList.add("d-none");
    modalCliente.show();
}

// Prepara el formulario para EDITAR un registro existente
function abrirModalEditar(id, nombre, email) {
    document.getElementById("formCliente").reset();
    document.getElementById("clienteId").value = id; // El ID oculto avisa que es un PUT
    document.getElementById("nombreCliente").value = nombre;
    document.getElementById("emailCliente").value = email;
    document.getElementById("tituloModal").textContent = "Editar Cliente #" + id;
    document.getElementById("alertaError").classList.add("d-none");
    modalCliente.show();
}

// Envía los datos (ya sea POST o PUT dependiendo del ID oculto)
function guardarCliente(evento) {
    evento.preventDefault();
    const alertaError = document.getElementById("alertaError");
    alertaError.classList.add("d-none");

    const id = document.getElementById("clienteId").value;
    const url = id ? `/api/clientes/${id}` : '/api/clientes';
    const method = id ? 'PUT' : 'POST';

    const clienteData = {
        nombre: document.getElementById("nombreCliente").value,
        email: document.getElementById("emailCliente").value
    };

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(clienteData)
    })
        .then(response => {
            if (response.ok) return response.json();
            return response.text().then(text => { throw new Error(text) });
        })
        .then(() => {
            modalCliente.hide();
            cargarClientes();
        })
        .catch(error => {
            alertaError.textContent = error.message;
            alertaError.classList.remove("d-none");
        });
}

function desactivarCliente(id) {
    if(confirm("¿Estás seguro de que deseas desactivar este cliente? No podrá realizar más alquileres.")) {
        fetch(`/api/clientes/${id}`, { method: 'DELETE' })
            .then(res => {
                if(res.ok) cargarClientes();
                else alert("No se pudo desactivar el cliente");
            });
    }
}