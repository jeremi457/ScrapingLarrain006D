package com.importadora.system.repository;

import com.importadora.system.model.ProductoScrapeado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoScrapeadoRepository extends JpaRepository<ProductoScrapeado, Long> {
}