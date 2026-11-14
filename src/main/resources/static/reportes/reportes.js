document.addEventListener("DOMContentLoaded", () => {
    const selectTipo = document.getElementById("tipoReporte");
    const inputBusqueda = document.getElementById("filtroBusqueda");
    const btnLimpiar = document.getElementById("btnLimpiarFiltros");
    const btnCargar = document.getElementById("btnCargarDatos");
    const btnPDF = document.getElementById("btnDescargarPDF");
    const btnPDFSecundario = document.getElementById("btnDescargarPDFSecundario");

    const inputTitulo = document.getElementById("inputTituloDoc");
    const inputResponsable = document.getElementById("inputResponsableDoc");
    const inputNotas = document.getElementById("inputNotasDoc");
    const inputMarcaAguaPrincipal = document.getElementById("inputMarcaAguaPrincipal");
    const inputMarcaAguaSub = document.getElementById("inputMarcaAguaSub");
    const chkKpis = document.getElementById("chkMostrarKpis");
    const chkNotas = document.getElementById("chkMostrarNotas");
    const chkMarcaAgua = document.getElementById("chkMostrarMarcaAgua");

    const mensajeEstado = document.getElementById("mensajeEstado");
    const badgeSincronizacion = document.getElementById("badgeSincronizacion");
    const tablaHead = document.getElementById("tablaHead");
    const tablaBody = document.getElementById("tablaBody");

    let datosOriginales = [];
    let datosFiltrados = [];
    const folioActual = "RL-" + new Date().getFullYear() + "-" + Math.floor(100 + Math.random() * 900);

    const columnasPorDefecto = {
        alquileres: ["ID", "CLIENTE", "EQUIPO", "FECHA INICIO", "TOTAL", "ESTADO"],
        clientes: ["ID", "NOMBRE", "DPI / NIT", "TELÉFONO", "CORREO", "ESTADO"],
        equipos: ["ID", "TIPO", "MODELO", "TARIFA / HORA", "BATERÍA", "ESTADO"],
        mantenimientos: ["ID", "EQUIPO", "DESCRIPCIÓN", "FECHA", "COSTO", "ESTADO"]
    };

    const formatearMoneda = (valor) => {
        return "Q " + Number(valor || 0).toLocaleString("es-GT", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    };

    const sincronizarEncabezadoDocumento = () => {
        const ahora = new Date();
        document.getElementById("prevFolioDoc").textContent = folioActual;
        document.getElementById("prevFechaDoc").textContent = ahora.toLocaleDateString("es-GT");
        document.getElementById("prevModuloDoc").textContent = selectTipo.value.toUpperCase();
        document.getElementById("prevTituloDoc").textContent = inputTitulo.value || "REPORTE OPERATIVO";
        document.getElementById("prevResponsableDoc").textContent = inputResponsable.value || "Rueda Libre";
        document.getElementById("prevNotasDoc").textContent = inputNotas.value || "";

        const prevMarcaPrincipal = document.getElementById("prevMarcaAguaPrincipal");
        const prevMarcaSub = document.getElementById("prevMarcaAguaSub");
        prevMarcaPrincipal.textContent = inputMarcaAguaPrincipal.value.toUpperCase();
        prevMarcaSub.textContent = inputMarcaAguaSub.value.toUpperCase();

        const mostrarMarca = chkMarcaAgua.checked;
        prevMarcaPrincipal.style.display = mostrarMarca ? "block" : "none";
        prevMarcaSub.style.display = mostrarMarca ? "block" : "none";
        inputMarcaAguaPrincipal.disabled = !mostrarMarca;
        inputMarcaAguaSub.disabled = !mostrarMarca;

        document.getElementById("bloqueKpisPrev").style.display = chkKpis.checked ? "flex" : "none";
        document.getElementById("bloqueNotasPrev").style.display = chkNotas.checked ? "block" : "none";
    };

    const calcularMetricas = () => {
        const total = datosFiltrados.length;
        let sumaMontos = 0;

        datosFiltrados.forEach(item => {
            const monto = item.total || item.monto || item.costo || item.tarifaHora || item.tarifa || item.saldoPendiente || 0;
            if (!isNaN(parseFloat(monto))) {
                sumaMontos += parseFloat(monto);
            }
        });

        const promedio = total > 0 ? sumaMontos / total : 0;

        document.getElementById("prevKpiMonto").textContent = formatearMoneda(sumaMontos);
        document.getElementById("prevKpiPromedio").textContent = formatearMoneda(promedio);
    };

    const dibujarTablaVaciaConEstructura = (textoFila) => {
        const tipo = selectTipo.value;
        const columnas = columnasPorDefecto[tipo] || ["ID", "DESCRIPCIÓN", "FECHA", "MONTO", "ESTADO"];

        tablaHead.innerHTML = "";
        tablaBody.innerHTML = "";

        const filaHeader = document.createElement("tr");
        columnas.forEach(col => {
            const th = document.createElement("th");
            th.textContent = col;
            filaHeader.appendChild(th);
        });
        tablaHead.appendChild(filaHeader);

        const trVacia = document.createElement("tr");
        const tdVacia = document.createElement("td");
        tdVacia.colSpan = columnas.length;
        tdVacia.className = "text-center text-muted py-4";
        tdVacia.textContent = textoFila;
        trVacia.appendChild(tdVacia);
        tablaBody.appendChild(trVacia);
    };

    const renderizarTabla = () => {
        if (!datosFiltrados || datosFiltrados.length === 0) {
            dibujarTablaVaciaConEstructura("Sin registros disponibles para mostrar en este reporte.");
            return;
        }

        mensajeEstado.textContent = "";
        tablaHead.innerHTML = "";
        tablaBody.innerHTML = "";

        const columnas = Object.keys(datosFiltrados[0]);

        const filaHeader = document.createElement("tr");
        columnas.forEach(col => {
            const th = document.createElement("th");
            th.textContent = col.toUpperCase();
            filaHeader.appendChild(th);
        });
        tablaHead.appendChild(filaHeader);

        datosFiltrados.forEach(item => {
            const tr = document.createElement("tr");
            columnas.forEach(col => {
                const td = document.createElement("td");
                let valor = item[col];
                if (typeof valor === "object" && valor !== null) {
                    valor = valor.nombre || valor.modelo || valor.id || JSON.stringify(valor);
                }
                td.textContent = valor !== null && valor !== undefined ? valor : "-";
                tr.appendChild(td);
            });
            tablaBody.appendChild(tr);
        });
    };

    const aplicarFiltros = () => {
        const termino = inputBusqueda.value.toLowerCase().trim();
        if (!termino) {
            datosFiltrados = [...datosOriginales];
        } else {
            datosFiltrados = datosOriginales.filter(item =>
                Object.values(item).some(val =>
                    String(val).toLowerCase().includes(termino)
                )
            );
        }
        calcularMetricas();
        renderizarTabla();
        sincronizarEncabezadoDocumento();
    };

    const cargarDatosReporte = async () => {
        const tipo = selectTipo.value;
        mensajeEstado.textContent = "";
        badgeSincronizacion.className = "badge bg-warning-subtle text-warning";
        badgeSincronizacion.textContent = "Sincronizando...";
        dibujarTablaVaciaConEstructura("Consultando registros...");

        try {
            const respuesta = await fetch(`/api/${tipo}`);
            if (!respuesta.ok) {
                throw new Error(`Error HTTP ${respuesta.status}`);
            }

            datosOriginales = await respuesta.json();
            badgeSincronizacion.className = "badge bg-success-subtle text-success";
            badgeSincronizacion.textContent = "Sincronizado";
            aplicarFiltros();
        } catch (error) {
            console.error("Error al obtener reporte:", error);
            datosOriginales = [];
            datosFiltrados = [];
            badgeSincronizacion.className = "badge bg-secondary-subtle text-secondary";
            badgeSincronizacion.textContent = "Vista Plantilla";
            calcularMetricas();
            dibujarTablaVaciaConEstructura("Documento listo • Sin registros cargados actualmente en la base de datos.");
        }
    };

    const descargarPDF = () => {
        const elemento = document.getElementById("areaImpresionPDF");
        const tipo = selectTipo.value;
        const opciones = {
            margin: 0.4,
            filename: `Reporte_RuedaLibre_${tipo}_${folioActual}.pdf`,
            image: { type: "jpeg", quality: 0.98 },
            html2canvas: { scale: 2 },
            jsPDF: { unit: "in", format: "letter", orientation: "portrait" }
        };
        html2pdf().set(opciones).from(elemento).save();
    };

    selectTipo.addEventListener("change", () => {
        inputBusqueda.value = "";
        cargarDatosReporte();
    });
    inputBusqueda.addEventListener("input", aplicarFiltros);
    btnLimpiar.addEventListener("click", () => {
        inputBusqueda.value = "";
        aplicarFiltros();
    });
    btnCargar.addEventListener("click", cargarDatosReporte);
    btnPDF.addEventListener("click", descargarPDF);
    btnPDFSecundario.addEventListener("click", descargarPDF);

    [inputTitulo, inputResponsable, inputNotas, inputMarcaAguaPrincipal, inputMarcaAguaSub, chkKpis, chkNotas, chkMarcaAgua].forEach(el => {
        el.addEventListener("input", sincronizarEncabezadoDocumento);
        el.addEventListener("change", sincronizarEncabezadoDocumento);
    });

    sincronizarEncabezadoDocumento();
    cargarDatosReporte();
});