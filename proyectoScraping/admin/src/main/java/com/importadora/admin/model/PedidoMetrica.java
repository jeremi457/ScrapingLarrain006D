package com.importadora.admin.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapea la tabla "pedido" de cliente en modo solo lectura.
 * Admin nunca escribe en esta tabla — solo la consulta para métricas.
 */
@Entity
@Table(name = "pedido")
public class PedidoMetrica {

    @Id
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(name = "rut")
    private String rut;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "total_pedido")
    private Double totalPedido;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pedido_item", joinColumns = @JoinColumn(name = "id_pedido"))
    private List<PedidoItemMetrica> items = new ArrayList<>();

    public Long getIdPedido()       { return idPedido; }
    public String getNombreCliente(){ return nombreCliente; }
    public String getRut()          { return rut; }
    public String getMetodoPago()   { return metodoPago; }
    public Double getTotalPedido()  { return totalPedido; }
    public List<PedidoItemMetrica> getItems() { return items; }
}