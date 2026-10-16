import { useEffect, useState } from 'react'
import './App.css'

const API = '/api/parking'

export default function App() {
  const [capacity, setCapacity] = useState(4)
  const [vehicles, setVehicles] = useState(10)
  const [s, setS] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const t = setInterval(async () => {
      try {
        const r = await fetch(`${API}/state`)
        setS(r.status === 200 ? await r.json() : null)
        setError('')
      } catch {
        setError('No hay conexión con el backend (¿está corriendo en el puerto 8080?)')
      }
    }, 300)
    return () => clearInterval(t)
  }, [])

  const iniciar = async () => {
    const r = await fetch(`${API}/start?capacity=${capacity}&vehicles=${vehicles}`, { method: 'POST' })
    setError(r.ok ? '' : await r.text())
  }
  const reiniciar = () => fetch(`${API}/reset`, { method: 'POST' })

  const lista = s?.vehiculos ?? []
  const dentro = lista.filter((v) => v.estado === 'DENTRO')
  const esperando = lista.filter((v) => v.estado === 'ESPERANDO')
  const fuera = lista.filter((v) => v.estado === 'FUERA')
  const slots = Array.from({ length: s?.capacidad ?? capacity }, (_, i) => dentro[i])

  return (
    <main>
      <h1>Estacionamiento — Semáforo contador</h1>
      <p className="sub">Sistemas Distribuidos · Práctica 3 · Ejercicio 1</p>

      <section className="controls">
        <label>
          Valor inicial del semáforo
          <input type="number" min="1" max="20" value={capacity}
                 onChange={(e) => setCapacity(+e.target.value)} />
        </label>
        <label>
          Vehículos
          <input type="number" min="1" max="30" value={vehicles}
                 onChange={(e) => setVehicles(+e.target.value)} />
        </label>
        <button onClick={iniciar} disabled={s?.corriendo}>Iniciar</button>
        <button className="sec" onClick={reiniciar}>Reiniciar</button>
      </section>

      {error && <p className="error">{error}</p>}

      {s && (
        <>
          <section className="stats">
            <div><b>{s.dentro}</b><span>Dentro ahora</span></div>
            <div><b>{s.maximoDentro}</b><span>Máximo dentro</span></div>
            <div><b>{s.esperando}</b><span>Bloqueados</span></div>
            <div><b>{(s.tiempoMs / 1000).toFixed(1)} s</b><span>Tiempo</span></div>
            <div className={s.cumple ? 'ok' : 'bad'}>
              <b>{s.cumple ? 'CUMPLE' : 'NO CUMPLE'}</b><span>Máx. dentro ≤ 4</span>
            </div>
          </section>

          <h2>Estacionamiento</h2>
          <div className="slots">
            {slots.map((v, i) => (
              <div key={i} className={`slot ${v ? 'busy' : ''}`}>{v ? `🚗 ${v.id}` : 'libre'}</div>
            ))}
          </div>

          <div className="cols">
            <div>
              <h2>En espera (wait bloqueado)</h2>
              <div className="chips">{esperando.map((v) => <span key={v.id} className="chip wait">{v.id}</span>)}</div>
            </div>
            <div>
              <h2>Ya salieron</h2>
              <div className="chips">{fuera.map((v) => <span key={v.id} className="chip done">{v.id}</span>)}</div>
            </div>
          </div>

          <h2>Registro de eventos</h2>
          <pre className="log">{[...s.eventos].reverse().join('\n')}</pre>
        </>
      )}
      {!s && !error && <p className="sub">Presiona “Iniciar” para lanzar la simulación.</p>}
    </main>
  )
}
