function cargarClientes() {
    // Apunta exactamente a la ruta definida @RequestMapping("/api/clientes")
    fetch('/api/clientes')
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al obtener los clientes');
            }
            return response.json();
        })
        .then(clientes => {
            const cuerpoTabla = document.getElementById('cuerpoTabla');
            cuerpoTabla.innerHTML = ''; // Limpiar la tabla antes de rellenar

            clientes.forEach(cliente => {
                const fila = document.createElement('tr');

                fila.innerHTML = `
                    <td>${cliente.idCliente}</td>
                    <td>${cliente.nombre}</td>
                    <td>${cliente.email}</td>
                    <td>$${cliente.saldoPendiente}</td>
                    <td>${cliente.estado}</td>
                `;

                cuerpoTabla.appendChild(fila);
            });
        })
        .catch(error => {
            console.error('Ocurrió un problema:', error);
            alert('No se pudieron cargar los clientes. Revisa la consola.');
        });
}