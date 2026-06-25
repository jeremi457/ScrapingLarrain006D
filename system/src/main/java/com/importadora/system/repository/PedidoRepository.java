package com.importadora.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.importadora.system.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
