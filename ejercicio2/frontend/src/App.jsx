import { useEffect, useState } from 'react'
import { consultarSaldo, simular } from './api.js'
import ResultadoPanel from './ResultadoPanel.jsx'

const SALDO_INICIAL = 1000

export default function App() {
  const [saldoActual, setSaldoActual] = useState(null)
  const [resultados, setResultados] = useState({ 'con-mutex': null, 'sin-mutex': null })
  const [ocupado, setOcupado] = useState(null) // 'saldo' | 'con-mutex' | 'sin-mutex' | null
  const [error, setError] = useState('')

  // Ejecuta una acción mostrando "cargando" y capturando errores.
  async function ejecutar(etiqueta, accion) {
    setOcupado(etiqueta)
    setError('')
    try {
      await accion()
    } catch (e) {
      setError(e.message)
    } finally {
      setOcupado(null)
    }
  }

  function consultar() {
    return ejecutar('saldo', async () => {
      const datos = await consultarSaldo()
      setSaldoActual(datos.saldo)
    })
  }

  function ejecutarSimulacion(modo) {
    return ejecutar(modo, async () => {
      const resultado = await simular(modo)
      setResultados((anteriores) => ({ ...anteriores, [modo]: resultado }))
      setSaldoActual(resultado.saldoFinal)
    })
  }

  useEffect(() => {
    consultar()
  }, [])

  const hayAlgoEnCurso = ocupado !== null

  return (
    <main className="pagina">
      <header className="encabezado">
        <h1>Cajero automático compartido</h1>
        <p>
          Tres procesos modifican a la vez la misma cuenta: el Proceso 1 retira $300, el Proceso 2 retira
          $250 y el Proceso 3 deposita $200. Compara qué pasa con y sin exclusión mutua.
        </p>
      </header>

      <section className="pantalla" aria-label="Estado de la cuenta">
        <div>
          <span className="etiqueta">Saldo inicial</span>
          <strong className="monto">${SALDO_INICIAL}</strong>
        </div>
        <div className="destacado">
          <span className="etiqueta">Saldo actual</span>
          <strong className="monto grande">{saldoActual === null ? '—' : `$${saldoActual}`}</strong>
        </div>
        <div>
          <span className="etiqueta">Saldo final esperado</span>
          <strong className="monto">$650</strong>
        </div>
      </section>

      <div className="acciones">
        <button className="btn btn-mutex" onClick={() => ejecutarSimulacion('con-mutex')} disabled={hayAlgoEnCurso}>
          Ejecutar con mutex
        </button>
        <button className="btn btn-sin" onClick={() => ejecutarSimulacion('sin-mutex')} disabled={hayAlgoEnCurso}>
          Ejecutar sin mutex
        </button>
        <button className="btn btn-borde" onClick={consultar} disabled={hayAlgoEnCurso}>
          Consultar saldo
        </button>
      </div>

      <div className="estado" aria-live="polite">
        {ocupado === 'saldo' && <p className="cargando">Consultando el saldo…</p>}
        {(ocupado === 'con-mutex' || ocupado === 'sin-mutex') && (
          <p className="cargando">
            Ejecutando la simulación {ocupado === 'con-mutex' ? 'con' : 'sin'} mutex. Espera a que terminen los
            tres procesos…
          </p>
        )}
        {error && (
          <p className="error" role="alert">
            {error}
          </p>
        )}
      </div>

      <div className="comparacion">
        <ResultadoPanel
          titulo="Con mutex"
          subtitulo="Cada proceso toma el lock, comprueba y modifica el saldo, y lo libera."
          clase="con"
          resultado={resultados['con-mutex']}
          ejecutando={ocupado === 'con-mutex'}
        />
        <ResultadoPanel
          titulo="Sin mutex"
          subtitulo="Cada proceso lee el saldo, espera un instante y escribe. Otro puede pisar su cambio."
          clase="sin"
          resultado={resultados['sin-mutex']}
          ejecutando={ocupado === 'sin-mutex'}
        />
      </div>

      <footer className="pie">
        El modo sin mutex es solo una demostración educativa. Que la condición de carrera aparezca depende de cada
        ejecución: si no aparece, vuelve a ejecutarlo.
      </footer>
    </main>
  )
}
