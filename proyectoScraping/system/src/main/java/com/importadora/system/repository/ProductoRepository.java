package com.importadora.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.importadora.system.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

}