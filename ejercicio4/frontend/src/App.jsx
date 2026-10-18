import React, { useState, useEffect } from 'react';

function App() {
  const [colaNorte, setColaNorte] = useState([]);
  const [colaSur, setColaSur] = useState([]);

  const [totalInicialNorte, setTotalInicialNorte] = useState(0);
  const [totalInicialSur, setTotalInicialSur] = useState(0);

  const [autosEnPuente, setAutosEnPuente] = useState([]);

  const [estadoPuente, setEstadoPuente] = useState({
    direccion: 'NINGUNA',
    vehiculosNorte: 0,
    vehiculosSur: 0,
    conteoConsecutivoNorte: 0,
    conteoConsecutivoSur: 0
  });

  const [registros, setRegistros] = useState([]);
  const [simulando, setSimulando] = useState(false);

  // Leer el estado del backend en tiempo real (cada 200ms)
  useEffect(() => {
    const intervalo = setInterval(async () => {
      try {
        const respuesta = await fetch('http://localhost:8080/api/puente/estado');
        if (respuesta.ok) {
          const datos = await respuesta.json();
          setEstadoPuente(datos);
        }
      } catch (e) {
        // Ignorar interrupciones temporales
      }
    }, 200);
    return () => clearInterval(intervalo);
  }, []);

  // Agregar vehículos al Norte (Máx. 12)
  const agregarNorte = () => {
    if (simulando) return;
    if (colaNorte.length >= 12) return;
    const nuevoId = `Carro-${colaNorte.length + 1}`;
    const nuevaCola = [...colaNorte, nuevoId];
    setColaNorte(nuevaCola);
    setTotalInicialNorte(nuevaCola.length);
  };

  // Agregar vehículos al Sur (Máx. 12)
  const agregarSur = () => {
    if (simulando) return;
    if (colaSur.length >= 12) return;
    const nuevoId = `Auto-${colaSur.length + 1}`;
    const nuevaCola = [...colaSur, nuevoId];
    setColaSur(nuevaCola);
    setTotalInicialSur(nuevaCola.length);
  };

  // Simulación por lotes estrictos de máx. 6 vehículos (Turnos Norte ➔ Sur ➔ Norte ➔ Sur)
  const iniciarSimulacion = async () => {
    if (colaNorte.length === 0 && colaSur.length === 0) {
      alert("Agrega vehículos en el Norte o Sur para comenzar.");
      return;
    }

    setSimulando(true);
    setRegistros(prev => ["--- INICIANDO SIMULACIÓN POR LOTES (MÁX. 6 POR GRUPO) ---", ...prev]);

    let qNorte = [...colaNorte];
    let qSur = [...colaSur];

    // Bucle principal mientras queden vehículos por cruzar en alguna de las dos colas
    while (qNorte.length > 0 || qSur.length > 0) {

      // --- 1. TURNO NORTE (Extrae un lote de hasta 6 vehículos) ---
      if (qNorte.length > 0) {
        const loteNorte = qNorte.splice(0, 6); // Extrae máximo 6
        setRegistros(prev => [`[NORTE] Turno activo: Procesando lote de ${loteNorte.length} vehículo(s)`, ...prev]);

        for (let i = 0; i < loteNorte.length; i++) {
          const id = loteNorte[i];

          // Reducir el contador visual exactamente en el momento de salida
          setColaNorte(prev => prev.filter(item => item !== id));

          // Activar la animación visual en el carril
          const autoKey = `NORTE-${id}-${Date.now()}`;
          setAutosEnPuente(prev => [...prev, { key: autoKey, tipo: 'NORTE', id }]);

          // Petición al backend (respeta mutex y exclusión mutua)
          fetch(`http://localhost:8080/api/puente/cruzar/norte?idVehiculo=${encodeURIComponent(id)}`, { method: 'POST' })
            .then(res => res.text())
            .then(mensaje => {
              setRegistros(prev => [mensaje, ...prev]);
            })
            .catch(() => {});

          // Remover del carril visual tras completar los 4 segundos de cruce
          setTimeout(() => {
            setAutosEnPuente(prev => prev.filter(a => a.key !== autoKey));
          }, 4000);

          // Pausa estricta de 2 segundos entre carro y carro
          await new Promise(r => setTimeout(r, 2000));
        }

        // Breve pausa al terminar el lote si el Sur tiene vehículos esperando
        if (qSur.length > 0) {
          setRegistros(prev => ["--- NORTE CEDE EL PASO AL SUR ---", ...prev]);
          await new Promise(r => setTimeout(r, 1000));
        }
      }

      // --- 2. TURNO SUR (Extrae un lote de hasta 6 vehículos) ---
      if (qSur.length > 0) {
        const loteSur = qSur.splice(0, 6); // Extrae máximo 6
        setRegistros(prev => [`[SUR] Turno activo: Procesando lote de ${loteSur.length} vehículo(s)`, ...prev]);

        for (let j = 0; j < loteSur.length; j++) {
          const id = loteSur[j];

          // Reducir el contador visual exactamente en el momento de salida
          setColaSur(prev => prev.filter(item => item !== id));

          // Activar la animación visual en el carril
          const autoKey = `SUR-${id}-${Date.now()}`;
          setAutosEnPuente(prev => [...prev, { key: autoKey, tipo: 'SUR', id }]);

          // Petición al backend
          fetch(`http://localhost:8080/api/puente/cruzar/sur?idVehiculo=${encodeURIComponent(id)}`, { method: 'POST' })
            .then(res => res.text())
            .then(mensaje => {
              setRegistros(prev => [mensaje, ...prev]);
            })
            .catch(() => {});

          setTimeout(() => {
            setAutosEnPuente(prev => prev.filter(a => a.key !== autoKey));
          }, 4000);

          // Pausa estricta de 2 segundos entre carro y carro
          await new Promise(r => setTimeout(r, 2000));
        }

        // Breve pausa al terminar el lote si el Norte tiene vehículos pendientes
        if (qNorte.length > 0) {
          setRegistros(prev => ["--- SUR CEDE EL PASO AL NORTE ---", ...prev]);
          await new Promise(r => setTimeout(r, 1000));
        }
      }
    }

    setSimulando(false);
    setRegistros(prev => ["--- SIMULACIÓN FINALIZADA CON ÉXITO ---", ...prev]);
  };

  return (
    <div style={{ fontFamily: 'Segoe UI, sans-serif', padding: '20px', maxWidth: '950px', margin: '0 auto', backgroundColor: '#f4f6f9' }}>
      
      {/* Animaciones CSS del carril */}
      <style>{`
        @keyframes cruzarIzquierdaADerecha {
          0% { left: -5%; opacity: 0; }
          10% { opacity: 1; }
          90% { opacity: 1; }
          100% { left: 95%; opacity: 0; }
        }
        @keyframes cruzarDerechaAIzquierda {
          0% { left: 95%; opacity: 0; }
          10% { opacity: 1; }
          90% { opacity: 1; }
          100% { left: -5%; opacity: 0; }
        }
      `}</style>

      <h2 style={{ textAlign: 'center', color: '#2c3e50' }}>🌉 Control de Acceso al Puente — Exclusión Mutua</h2>
      <p style={{ textAlign: 'center', color: '#7f8c8d', marginTop: '-10px' }}>Simulación por Lotes Académicos (Máx. 6 por sentido con alternancia automática)</p>

      {/* ÁREA DEL PUENTE Y CONTADORES */}
      <div style={{ backgroundColor: '#2c3e50', borderRadius: '12px', padding: '20px', color: 'white', marginBottom: '20px' }}>
        
        <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid #455a64', paddingBottom: '10px', marginBottom: '15px' }}>
          <span><strong>Dirección Activa:</strong> {estadoPuente.direccion === 'NINGUNA' ? '🟢 LIBRE' : `🔴 ${estadoPuente.direccion}`}</span>
          <span><strong>Consecutivos en Turno:</strong> {estadoPuente.direccion === 'NORTE' ? estadoPuente.conteoConsecutivoNorte : estadoPuente.conteoConsecutivoSur} / 6</span>
        </div>

        {/* ESTRUCTURA DEL CARRIL ÚNICO */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          
          {/* CONTADOR IZQUIERDO (NORTE) */}
          <div style={{
            backgroundColor: '#007bff',
            padding: '15px 20px',
            borderRadius: '8px',
            textAlign: 'center',
            fontWeight: 'bold',
            minWidth: '95px',
            boxShadow: '0 4px 6px rgba(0,0,0,0.2)'
          }}>
            <div style={{ fontSize: '0.8em' }}>NORTE</div>
            <div style={{ fontSize: '1.4em' }}>{colaNorte.length} / {totalInicialNorte}</div>
          </div>

          {/* CARRIL COMPARTIDO */}
          <div style={{
            flex: 1,
            height: '80px',
            backgroundColor: '#34495e',
            borderRadius: '8px',
            position: 'relative',
            overflow: 'hidden',
            border: '3px solid #16a085'
          }}>
            <div style={{ position: 'absolute', top: '50%', width: '100%', borderTop: '2px dashed #f1c40f' }}></div>

            {/* VEHÍCULOS EN ANIMACIÓN */}
            {autosEnPuente.map((auto) => (
              <div 
                key={auto.key}
                style={{
                  position: 'absolute',
                  top: '25px',
                  backgroundColor: auto.tipo === 'NORTE' ? '#007bff' : '#dc3545',
                  width: '50px',
                  height: '30px',
                  borderRadius: '6px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '0 2px 6px rgba(0,0,0,0.4)',
                  fontSize: '1.4em',
                  animation: auto.tipo === 'NORTE' 
                    ? 'cruzarIzquierdaADerecha 4s linear forwards' 
                    : 'cruzarDerechaAIzquierda 4s linear forwards'
                }}>
                {auto.tipo === 'NORTE' ? '🚗' : '🚙'}
              </div>
            ))}

            {autosEnPuente.length === 0 && (
              <div style={{ position: 'absolute', top: '30px', width: '100%', textAlign: 'center', color: '#bdc3c7', fontStyle: 'italic', fontSize: '0.9em' }}>
                Puente Despejado
              </div>
            )}
          </div>

          {/* CONTADOR DERECHO (SUR) */}
          <div style={{
            backgroundColor: '#dc3545',
            padding: '15px 20px',
            borderRadius: '8px',
            textAlign: 'center',
            fontWeight: 'bold',
            minWidth: '95px',
            boxShadow: '0 4px 6px rgba(0,0,0,0.2)'
          }}>
            <div style={{ fontSize: '0.8em' }}>SUR</div>
            <div style={{ fontSize: '1.4em' }}>{colaSur.length} / {totalInicialSur}</div>
          </div>

        </div>

      </div>

      {/* CONTROLES PARA AGREGAR VEHÍCULOS */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        
        {/* PANEL NORTE */}
        <div style={{ backgroundColor: '#eef6ff', border: '2px solid #007bff', borderRadius: '10px', padding: '15px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 style={{ color: '#007bff', margin: 0 }}>🚗 Grupo Norte (Azules)</h3>
            <button 
              onClick={agregarNorte}
              disabled={simulando}
              style={{ backgroundColor: '#007bff', color: 'white', border: 'none', padding: '8px 14px', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', opacity: simulando ? 0.6 : 1 }}>
              + Agregar Carro
            </button>
          </div>
          <p style={{ margin: '10px 0 0 0', color: '#555', fontSize: '0.9em' }}>En cola: <strong>{colaNorte.length}</strong> (Máx. 12)</p>
        </div>

        {/* PANEL SUR */}
        <div style={{ backgroundColor: '#fdeaea', border: '2px solid #dc3545', borderRadius: '10px', padding: '15px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 style={{ color: '#dc3545', margin: 0 }}>🚙 Grupo Sur (Rojos)</h3>
            <button 
              onClick={agregarSur}
              disabled={simulando}
              style={{ backgroundColor: '#dc3545', color: 'white', border: 'none', padding: '8px 14px', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', opacity: simulando ? 0.6 : 1 }}>
              + Agregar Auto
            </button>
          </div>
          <p style={{ margin: '10px 0 0 0', color: '#555', fontSize: '0.9em' }}>En cola: <strong>{colaSur.length}</strong> (Máx. 12)</p>
        </div>

      </div>

      {/* BOTÓN INICIAR */}
      <div style={{ textAlign: 'center', marginBottom: '20px' }}>
        <button 
          onClick={iniciarSimulacion}
          disabled={simulando}
          style={{ backgroundColor: '#28a745', color: 'white', border: 'none', padding: '12px 35px', fontSize: '1.1em', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold', opacity: simulando ? 0.6 : 1 }}>
          {simulando ? '⏳ Simulación en Curso...' : '🚀 Iniciar Simulación'}
        </button>
      </div>

      {/* CONSOLA DE LOGS */}
      <div style={{ backgroundColor: '#1e1e1e', color: '#00ff00', padding: '15px', borderRadius: '8px', height: '150px', overflowY: 'auto', fontFamily: 'monospace', fontSize: '0.85em' }}>
        <strong>Consola de Eventos y Mutex:</strong>
        {registros.map((r, i) => (
          <div key={i} style={{ marginTop: '4px' }}>&gt; {r}</div>
        ))}
      </div>

    </div>
  );
}

export default App;
