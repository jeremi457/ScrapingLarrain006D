package com.importadora.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "producto_scrapeado")
public class ProductoScrapeado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idScrapeo;

    @Column(nullable = false, length = 500)
    private String nombreProducto;

    @Column(nullable = false)
    private Double precioLocal; 

    @Column(nullable = false)
    private String tipoProducto;

    @Column(nullable = false)
    private LocalDateTime fechaScrapeo = LocalDateTime.now();

    public ProductoScrapeado(String nombreProducto, Double precioLocal, String tipoProducto) {
        this.nombreProducto = nombreProducto;
        this.precioLocal = precioLocal;
        this.tipoProducto = tipoProducto;
        this.fechaScrapeo = LocalDateTime.now();
    }
}