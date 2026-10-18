// Llamadas al microservicio cajero-service (vía el proxy de Vite).
const BASE = '/api/cajero'

async function pedir(ruta, opciones) {
  let respuesta
  try {
    respuesta = await fetch(BASE + ruta, opciones)
  } catch {
    throw new Error(
      'No se pudo conectar con el backend. Verifica que cajero-service esté corriendo en el puerto 8080.',
    )
  }

  let datos = null
  try {
    datos = await respuesta.json()
  } catch {
    // la respuesta no tenía cuerpo JSON
  }

  if (!respuesta.ok) {
    throw new Error(datos?.error ?? `El servidor respondió con el error ${respuesta.status}.`)
  }
  return datos
}

export function consultarSaldo() {
  return pedir('/saldo')
}

// modo: 'con-mutex' o 'sin-mutex'
export function simular(modo) {
  return pedir(`/simular/${modo}`, { method: 'POST' })
}
