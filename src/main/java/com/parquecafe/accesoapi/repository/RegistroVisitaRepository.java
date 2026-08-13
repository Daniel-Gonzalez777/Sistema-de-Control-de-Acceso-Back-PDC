package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.RegistroVisita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroVisitaRepository extends JpaRepository<RegistroVisita, Long> {

    // Visitas que aún no tienen hora de salida registrada (visitantes que siguen dentro del Parque)
    List<RegistroVisita> findByFechaHoraSalidaIsNull();
}
