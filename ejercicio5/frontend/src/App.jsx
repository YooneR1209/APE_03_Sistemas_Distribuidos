import { useEffect, useState } from 'react'
import './App.css'

const API = '/api/impresion'
const ETAPAS = ['Diseño', 'Revisión', 'Impresión']
const PREFIJO = ['D', 'R', 'I']

const celda = (v) => (v === 2 ? '✔ hecho' : v === 1 ? '⏳ en curso' : '—')

export default function App() {
  const [docs, setDocs] = useState(3)
  const [omitir, setOmitir] = useState('')
  const [s, setS] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const t = setInterval(async () => {
      try {
        const r = await fetch(`${API}/state`)
        setS(r.status === 200 ? await r.json() : null)
        setError('')
      } catch {
        setError('No hay conexión con el backend (¿está corriendo en el puerto 8081?)')
      }
    }, 300)
    return () => clearInterval(t)
  }, [])

  const iniciar = async () => {
    const q = `docs=${docs}${omitir ? `&omitir=${omitir}` : ''}`
    const r = await fetch(`${API}/start?${q}`, { method: 'POST' })
    setError(r.ok ? '' : await r.text())
  }
  const reiniciar = () => fetch(`${API}/reset`, { method: 'POST' })

  const estado = !s ? '' : s.terminado ? 'COMPLETADO' : s.bloqueado ? 'BLOQUEADO (deadlock)' : 'EN EJECUCIÓN'
  const claseEstado = !s ? '' : s.terminado ? 'ok' : s.bloqueado ? 'bad' : 'run'

  return (
    <main>
      <h1>Centro de impresión — Semáforos</h1>
      <p className="sub">Sistemas Distribuidos · Práctica 3 · Ejercicio 5 · Diseño → Revisión → Impresión</p>

      <section className="controls">
        <label>
          Documentos
          <input type="number" min="1" max="10" value={docs} onChange={(e) => setDocs(+e.target.value)} />
        </label>
        <label>
          Eliminar un signal() (análisis)
          <select value={omitir} onChange={(e) => setOmitir(e.target.value)}>
            <option value="">Ninguno (correcto)</option>
            <option value="DISENO">Diseño → signal(turno_revision)</option>
            <option value="REVISION">Revisión → signal(turno_impresion)</option>
            <option value="IMPRESION">Impresión → signal(turno_diseno)</option>
          </select>
        </label>
        <button onClick={iniciar} disabled={s?.corriendo}>Iniciar</button>
        <button className="sec" onClick={reiniciar}>Reiniciar</button>
      </section>

      {error && <p className="error">{error}</p>}

      {s && (
        <>
          <section className="stats">
            <div className={claseEstado}><b>{estado}</b><span>Estado</span></div>
            <div className={s.ordenCorrecto ? 'ok' : 'bad'}>
              <b>{s.ordenCorrecto ? 'CORRECTO' : 'INCORRECTO'}</b><span>Orden D→R→I</span>
            </div>
            <div><b>{(s.tiempoMs / 1000).toFixed(1)} s</b><span>Tiempo</span></div>
          </section>

          <h2>Documentos por etapa</h2>
          <table>
            <thead>
              <tr><th>Documento</th>{ETAPAS.map((e) => <th key={e}>{e}</th>)}</tr>
            </thead>
            <tbody>
              {s.matriz.map((fila, i) => (
                <tr key={i}>
                  <td>Doc {i + 1}</td>
                  {fila.map((v, j) => <td key={j} className={`c${v}`}>{celda(v)}</td>)}
                </tr>
              ))}
            </tbody>
          </table>

          <h2>Secuencia de ejecución</h2>
          <div className="chips">
            {s.secuencia.map((x, i) => (
              <span key={i} className={`chip ${PREFIJO.indexOf(x[0])}`}>{x}</span>
            ))}
          </div>

          <h2>Registro de eventos</h2>
          <pre className="log">{[...s.eventos].reverse().join('\n')}</pre>
        </>
      )}
      {!s && !error && <p className="sub">Presiona “Iniciar” para lanzar la simulación.</p>}
    </main>
  )
}
