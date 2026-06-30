package com.importadora.system.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class PedidoItem {
    
    private Long idProductoOriginal; 
    private String nombreProducto;   
    private Double precioCobrado;    
    private Integer cantidad;
}