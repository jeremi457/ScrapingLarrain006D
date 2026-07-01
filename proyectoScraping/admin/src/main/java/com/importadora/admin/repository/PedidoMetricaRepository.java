package com.importadora.admin.repository;

import com.importadora.admin.model.PedidoMetrica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PedidoMetricaRepository extends JpaRepository<PedidoMetrica, Long> {
    List<PedidoMetrica> findAllByOrderByRutAscIdPedidoAsc();
}