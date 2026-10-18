package com.puente.backend.servicio;

import com.puente.backend.modelo.Puente;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PuenteService {

    // MUTEX mutex = 1
    private final Lock mutex = new ReentrantLock();

    // Estado del recurso compartido (direccion = NINGUNA, vehiculos_norte = 0, vehiculos_sur = 0)
    private final Puente estado = new Puente();

    // RETO: Evitar que una dirección monopolice el puente (Máximo 6 vehículos seguidos)
    private static final int MAX_CONSECUTIVOS = 6;

    public Puente obtenerEstado() {
        mutex.lock();
        try {
            return estado;
        } finally {
            mutex.unlock();
        }
    }

    // PROCEDIMIENTO vehiculo_norte(id)
    public String vehiculoNorte(String id) throws InterruptedException {
        // lock(mutex)
        mutex.lock();
        try {
            // MIENTRAS direccion == SUR (o se cumplió el límite de 6 y hay vehículos esperando en el SUR) HACER esperar()
            while (estado.getDireccion().equals("SUR") || 
                  (estado.getConteoConsecutivoNorte() >= MAX_CONSECUTIVOS && estado.getVehiculosSur() > 0)) {
                mutex.unlock();
                Thread.sleep(200); // esperar()
                mutex.lock();
            }

            // direccion = NORTE
            estado.setDireccion("NORTE");
            // vehiculos_norte = vehiculos_norte + 1
            estado.setVehiculosNorte(estado.getVehiculosNorte() + 1);
            
            // Contador del reto anti-monopolización
            estado.setConteoConsecutivoNorte(estado.getConteoConsecutivoNorte() + 1);
            estado.setConteoConsecutivoSur(0);

        } finally {
            // unlock(mutex)
            mutex.unlock();
        }

        // escribir("Vehículo ", id, " cruza hacia el NORTE") -> cruzar_puente(id)
        Thread.sleep(4000); 

        // lock(mutex)
        mutex.lock();
        try {
            // vehiculos_norte = vehiculos_norte - 1
            estado.setVehiculosNorte(estado.getVehiculosNorte() - 1);

            // SI vehiculos_norte == 0 ENTONCES direccion = NINGUNA
            if (estado.getVehiculosNorte() == 0) {
                estado.setDireccion("NINGUNA");
            }
        } finally {
            // unlock(mutex)
            mutex.unlock();
        }

        return "Vehículo " + id + " cruza hacia el NORTE";
    }

    // PROCEDIMIENTO vehiculo_sur(id) (Completado siguiendo la misma lógica del pseudocódigo)
    public String vehiculoSur(String id) throws InterruptedException {
        // lock(mutex)
        mutex.lock();
        try {
            // MIENTRAS direccion == NORTE (o se cumplió el límite de 6 y hay vehículos esperando en el NORTE) HACER esperar()
            while (estado.getDireccion().equals("NORTE") || 
                  (estado.getConteoConsecutivoSur() >= MAX_CONSECUTIVOS && estado.getVehiculosNorte() > 0)) {
                mutex.unlock();
                Thread.sleep(200); // esperar()
                mutex.lock();
            }

            // direccion = SUR
            estado.setDireccion("SUR");
            // vehiculos_sur = vehiculos_sur + 1
            estado.setVehiculosSur(estado.getVehiculosSur() + 1);
            
            // Contador del reto anti-monopolización
            estado.setConteoConsecutivoSur(estado.getConteoConsecutivoSur() + 1);
            estado.setConteoConsecutivoNorte(0);

        } finally {
            // unlock(mutex)
            mutex.unlock();
        }

        // escribir("Vehículo ", id, " cruza hacia el SUR") -> cruzar_puente(id)
        Thread.sleep(4000);

        // lock(mutex)
        mutex.lock();
        try {
            // vehiculos_sur = vehiculos_sur - 1
            estado.setVehiculosSur(estado.getVehiculosSur() - 1);

            // SI vehiculos_sur == 0 ENTONCES direccion = NINGUNA
            if (estado.getVehiculosSur() == 0) {
                estado.setDireccion("NINGUNA");
            }
        } finally {
            // unlock(mutex)
            mutex.unlock();
        }

        return "Vehículo " + id + " cruza hacia el SUR";
    }
}
