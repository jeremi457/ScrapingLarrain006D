package com.importadora.system.controller;

import com.importadora.system.dto.ProductoPublicadoDTO;
import com.importadora.system.model.Producto;
import com.importadora.system.repository.ProductoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;


    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }


    @PostMapping
    public ResponseEntity<Void> recibirNuevoProducto(@RequestBody ProductoPublicadoDTO dto) {
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombreProducto(dto.nombreProducto());
        nuevoProducto.setPrecioLocal(dto.precioLocal()); 
        nuevoProducto.setTipoProducto(dto.tipoProducto());
        nuevoProducto.setStockBodega((int) (Math.random() * 20) + 5); 

        productoRepository.save(nuevoProducto);
        return ResponseEntity.ok().build();
    }
}