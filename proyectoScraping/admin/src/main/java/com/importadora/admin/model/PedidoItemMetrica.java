package com.importadora.admin.model;

import jakarta.persistence.*;

@Embeddable
public class PedidoItemMetrica {

    @Column(name = "nombre_producto")
    private String nombreProducto;

    @Column(name = "precio_cobrado")
    private Double precioCobrado;

    @Column(name = "cantidad")
    private Integer cantidad;

    public String getNombreProducto() { return nombreProducto; }
    public Double getPrecioCobrado()  { return precioCobrado; }
    public Integer getCantidad()      { return cantidad; }
}
