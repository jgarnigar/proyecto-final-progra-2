let modalAccesorio;

document.addEventListener("DOMContentLoaded", () => {
    modalAccesorio = new bootstrap.Modal(document.getElementById('modalAccesorio'));
    cargarAccesorios();
    document.getElementById("formAccesorio").addEventListener("submit", guardarAccesorio);
});

function cargarAccesorios() {
    fetch('/api/accesorios')
        .then(res => { if (!res.ok) throw new Error('Error de servidor'); return res.json(); })
        .then(accesorios => {
            const tbody = document.getElementById("cuerpoTablaAccesorios");
            tbody.innerHTML = accesorios.length ? "" : `<tr><td colspan="6" class="text-center">No hay accesorios.</td></tr>`;

            accesorios.forEach(acc => {
                const stockColor = acc.existenciaActual <= 5 ? "stock-bajo" : "";

                tbody.innerHTML += `
                    <tr>
                        <td class="fw-bold">#${acc.idAccesorio}</td>
                        <td>${acc.nombre}</td>
                        <td>Q ${acc.tarifaFija.toFixed(2)}</td>
                        <td>Q ${acc.precioReposicion.toFixed(2)}</td>
                        <td class="${stockColor}">${acc.existenciaActual}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-primary" onclick="abrirModalEditar(${acc.idAccesorio}, '${acc.nombre}', ${acc.tarifaFija}, ${acc.precioReposicion}, ${acc.existenciaActual})"><i class="bi bi-pencil"></i></button>
                            <button class="btn btn-sm btn-outline-danger" onclick="desactivarAccesorio(${acc.idAccesorio})"><i class="bi bi-trash"></i></button>
                        </td>
                    </tr>
                `;
            });
        }).catch(err => console.error(err));
}

function abrirModalCrear() {
    document.getElementById("formAccesorio").reset();
    document.getElementById("accesorioId").value = "";
    document.getElementById("tituloModal").textContent = "Nuevo Accesorio";
    document.getElementById("alertaError").classList.add("d-none");
    modalAccesorio.show();
}

function abrirModalEditar(id, nombre, tarifa, repo, existencia) {
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

    // Mapeo exacto a las variables de tu clase Java
    const data = {
        nombre: document.getElementById("nombreAcc").value,
        tarifaFija: parseFloat(document.getElementById("tarifaAcc").value),
        precioReposicion: parseFloat(document.getElementById("reposicionAcc").value),
        existenciaActual: parseInt(document.getElementById("stockAcc").value)
    };

    fetch(url, { method: metodo, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) })
        .then(res => { if(res.ok) return res.json(); return res.text().then(t => { throw new Error(t) }); })
        .then(() => { modalAccesorio.hide(); cargarAccesorios(); })
        .catch(err => {
            const alerta = document.getElementById("alertaError");
            alerta.textContent = err.message; alerta.classList.remove("d-none");
        });
}

function desactivarAccesorio(id) {
    if(confirm("¿Retirar todo el stock (poner en 0) para este accesorio?")) {
        fetch(`/api/accesorios/${id}`, { method: 'DELETE' }).then(res => { if(res.ok) cargarAccesorios(); });
    }
}