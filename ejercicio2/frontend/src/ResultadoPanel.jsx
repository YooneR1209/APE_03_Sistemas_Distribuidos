// Muestra el resultado de un modo (con o sin mutex): saldo final, verificación y eventos.
export default function ResultadoPanel({ titulo, subtitulo, clase, resultado, ejecutando }) {
  return (
    <section className={`panel ${clase}`} aria-label={titulo}>
      <header className="panel-cabecera">
        <h2>{titulo}</h2>
        <p>{subtitulo}</p>
      </header>

      {ejecutando && <p className="vacio">Ejecutando los tres procesos…</p>}

      {!ejecutando && !resultado && (
        <p className="vacio">Todavía no ejecutaste este modo. Usa el botón de arriba para ver el resultado.</p>
      )}

      {!ejecutando && resultado && (
        <>
          <div className="final">
            <div>
              <span className="etiqueta">Saldo final</span>
              <strong className="monto">${resultado.saldoFinal}</strong>
            </div>
            <div>
              <span className="etiqueta">Saldo esperado</span>
              <strong className="monto suave">${resultado.saldoEsperado}</strong>
            </div>
          </div>

          <p className={`veredicto ${resultado.correcto ? 'bien' : 'mal'}`} role="status">
            {resultado.verificacion}
          </p>

          <h3>Operaciones</h3>
          <ul className="operaciones">
            {resultado.operaciones.map((op) => (
              <li key={op.proceso}>
                <span>{op.proceso}</span>
                <span>
                  {op.tipo === 'RETIRO' ? 'Retiro' : 'Depósito'} de ${op.monto}
                </span>
                <span className={op.realizada ? 'bien' : 'mal'}>
                  {op.realizada ? 'Realizada' : 'Saldo insuficiente'}
                </span>
              </li>
            ))}
          </ul>

          <h3>Eventos en orden de ocurrencia</h3>
          <ol className="eventos">
            {resultado.eventos.map((e) => (
              <li key={e.orden} className={`proceso-${e.proceso.slice(-1)}`}>
                <span className="tiempo">{e.tiempoMs.toFixed(2)} ms</span>
                <span className="quien">{e.proceso}</span>
                <span className="mensaje">{e.mensaje}</span>
                {e.saldo !== null && <span className="saldo-evento">${e.saldo}</span>}
              </li>
            ))}
          </ol>
        </>
      )}
    </section>
  )
}
