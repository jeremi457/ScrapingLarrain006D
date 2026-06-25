package com.importadora.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.importadora.system.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Modifying
    @Query(value = "TRUNCATE TABLE producto", nativeQuery = true)
    void vaciarYReiniciarIds();
}