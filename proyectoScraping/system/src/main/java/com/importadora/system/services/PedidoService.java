package com.importadora.system.services;

import com.importadora.system.model.Pedido;
import com.importadora.system.model.PedidoItem;
import com.importadora.system.model.Producto;
import com.importadora.system.repository.PedidoRepository;
import com.importadora.system.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    public void procesarPago(List<Producto> carrito, String nombreCliente, String rut,
                              String email, String direccion, String metodoPago) {
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setNombreCliente(nombreCliente);
        nuevoPedido.setRut(rut);
        nuevoPedido.setEmail(email);
        nuevoPedido.setDireccion(direccion);
        nuevoPedido.setMetodoPago(metodoPago);

        double total = 0;
        List<PedidoItem> items = new ArrayList<>();

        for (Producto p : carrito) {
            double precioFinal = p.getPrecioAlPublico(); 
            total += precioFinal;

            PedidoItem item = new PedidoItem();
            item.setIdProductoOriginal(p.getIdProducto());
            item.setNombreProducto(p.getNombreProducto());
            item.setPrecioCobrado(precioFinal);
            item.setCantidad(1);
            items.add(item);

            // Descontar stock
            p.setStockBodega(p.getStockBodega() - 1);
            productoRepository.save(p);
        }

        nuevoPedido.setTotalPedido(Math.round(total * 100.0) / 100.0);
        nuevoPedido.setItems(items);
        pedidoRepository.save(nuevoPedido);
    }
}