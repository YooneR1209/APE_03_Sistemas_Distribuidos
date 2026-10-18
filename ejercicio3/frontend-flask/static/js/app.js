const RUTAS = {
    estado: "/api/simulacion/estado",
    eventos: "/api/eventos?limite=150",
    iniciar: "/api/simulacion/iniciar",
    detener: "/api/simulacion/detener",
    reiniciar: "/api/simulacion/reiniciar",
    metricas: "/api/metricas/resumen",
};

const COLORES_EVENTO = {
    PRODUCIDO: "chip-verde", CONSUMIDO: "chip-azul", ANOMALIA: "chip-rojo",
    INICIO: "chip-violeta", FIN: "chip-ambar", INFO: "chip-gris", INTERRUPCION: "chip-naranja",
};

let capacidadActual = 5;
const $ = (id) => document.getElementById(id);

async function pedir(ruta, opciones = {}) {
    try {
        const r = await fetch(ruta, opciones);
        return { ok: r.ok, datos: await r.json().catch(() => ({})) };
    } catch (e) {
        return { ok: false, datos: { error: "Sin conexión con el servidor Flask" } };
    }
}

function mostrarError(msg) { $("banner-error").textContent = "⚠ " + msg; $("banner-error").classList.remove("oculto"); }
function ocultarError() { $("banner-error").classList.add("oculto"); }

function pintarAlmacen(estado) {
    const contenedor = $("slots");
    if (estado.capacidad !== capacidadActual) {
        capacidadActual = estado.capacidad;
        contenedor.innerHTML = "";
        for (let i = 0; i < capacidadActual; i++) {
            const ranura = document.createElement("div");
            ranura.className = "ranura";
            contenedor.appendChild(ranura);
        }
    }
    const piezas = estado.almacen || [];
    [...contenedor.children].forEach((ranura, i) => {
        ranura.classList.toggle("ocupada", i < piezas.length);
        ranura.textContent = i < piezas.length ? piezas[i] : "";
    });
    $("nivel-almacen").textContent = `${estado.nivelAlmacen} / ${estado.capacidad}`;
}

function pintarHilos(estado) {
    const contenedor = $("lista-hilos");
    contenedor.innerHTML = "";
    (estado.hilos || []).forEach((h) => {
        const clase = h.estado === "RUNNABLE" ? "chip-verde"
            : h.estado.includes("WAITING") ? "chip-ambar"
            : h.estado === "TERMINATED" ? "chip-gris" : "chip-azul";
        const tarjeta = document.createElement("div");
        tarjeta.className = "hilo";
        tarjeta.innerHTML = `
            <span class="rol ${h.rol === "PRODUCTOR" ? "rol-productor" : "rol-consumidor"}">${h.rol}</span>
            <strong>${h.nombre}</strong>
            <span>${h.procesadas} pieza(s)</span>
            <span class="chip ${clase}">${h.estado}</span>`;
        contenedor.appendChild(tarjeta);
    });
}

function pintarEventos(eventos) {
    const cuerpo = $("tabla-eventos").querySelector("tbody");
    cuerpo.innerHTML = "";
    eventos.forEach((e) => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td>${e.id}</td>
            <td>${new Date(e.timestamp).toLocaleTimeString()}</td>
            <td><span class="chip ${COLORES_EVENTO[e.tipo] || "chip-gris"}">${e.tipo}</span></td>
            <td>${e.actor}</td>
            <td>${e.nivelAlmacen >= 0 ? `${e.nivelAlmacen}/${e.capacidad}` : "—"}</td>
            <td>${e.mensaje}</td>`;
        cuerpo.appendChild(fila);
    });
}

async function actualizar() {
    const rEstado = await pedir(RUTAS.estado);
    const rMetricas = await pedir(RUTAS.metricas);
    const rEventos = await pedir(RUTAS.eventos);

    const indicador = $("estado-servicios");
    indicador.textContent = rEstado.ok ? "● Microservicios en línea" : "● Sin conexión con simulacion-service";
    indicador.className = "estado-servicios " + (rEstado.ok ? "en-linea" : "fuera-linea");

    if (!rEstado.ok) {
        mostrarError(rEstado.datos.error || "No se pudo contactar simulacion-service (puerto 8080). ¿Está en ejecución?");
        return;
    }
    ocultarError();

    const estado = rEstado.datos;
    pintarAlmacen(estado);
    pintarHilos(estado);
    $("estado-simulacion").textContent =
        estado.enEjecucion ? "▶ Simulación en ejecución…"
        : estado.finalizada ? "✔ Simulación finalizada"
        : "Sin simulación. Configure los parámetros y presione Iniciar.";

    if (rMetricas.ok) {
        const m = rMetricas.datos;
        $("m-producidas").textContent = m.producidas;
        $("m-consumidas").textContent = m.consumidas;
        $("m-almacen").textContent = m.enAlmacen;
        $("m-anomalias").textContent = m.anomalias;
        $("nota-balance").textContent = m.duracionSegundos > 0
            ? `Balance = ${m.balance} (0 ⇒ todo lo producido fue consumido) · ${m.duracionSegundos}s · Prod/s = ${m.produccionPorSegundo} · Cons/s = ${m.consumoPorSegundo}`
            : "Aún no hay eventos registrados.";
    }
    if (rEventos.ok) pintarEventos(rEventos.datos);
}

function leerConfiguracion() {
    return {
        numProductores: Number($("num-productores").value),
        numConsumidores: Number($("num-consumidores").value),
        iteraciones: Number($("iteraciones").value),
        capacidad: Number($("capacidad").value),
        inicialEspacios: Number($("inicial-espacios").value),
        inicialElementos: Number($("inicial-elementos").value),
    };
}

async function iniciar() {
    ocultarError();
    const r = await pedir(RUTAS.iniciar, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(leerConfiguracion()),
    });
    if (!r.ok) mostrarError(r.datos.error || "No se pudo iniciar la simulación");
    else actualizar();
}

async function detener() { await pedir(RUTAS.detener, { method: "POST" }); actualizar(); }

async function reiniciar() {
    await pedir(RUTAS.reiniciar, { method: "POST" });
    $("tabla-eventos").querySelector("tbody").innerHTML = "";
    capacidadActual = -1;
    actualizar();
}

$("btn-iniciar").addEventListener("click", iniciar);
$("btn-detener").addEventListener("click", detener);
$("btn-reiniciar").addEventListener("click", reiniciar);
$("capacidad").addEventListener("input", () => { $("inicial-espacios").value = $("capacidad").value; });

actualizar();
setInterval(actualizar, 800);
