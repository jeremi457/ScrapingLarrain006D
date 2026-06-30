package com.importadora.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(nullable = false, length = 500)
    private String nombreProducto;

    @Column(nullable = false)
    private Double precioLocal; // El costo base original en China

    @Column(nullable = false)
    private String tipoProducto;

    @Column(nullable = false)
    private Integer stockBodega;

    
    public Double getPrecioAlPublico() {
        if (this.precioLocal == null) return 0.0;
        return Math.round((this.precioLocal * 1.50) * 100.0) / 100.0;
    }
}