package com.importadora.admin.repository;
 
import com.importadora.admin.model.ProductoScrapeado;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface ProductoScrapeadoRepository extends JpaRepository<ProductoScrapeado, Long> {
}
