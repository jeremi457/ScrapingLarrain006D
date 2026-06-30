package com.importadora.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor 
@Data
@Entity
@Table(name="pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;

    private String nombreCliente;
    private String rut;
    private String email;
    private String direccion;
    private String metodoPago;
    private Double totalPedido;

    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pedido_item", joinColumns = @JoinColumn(name = "id_pedido"))
    private List<PedidoItem> items = new ArrayList<>();
}