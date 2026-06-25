package com.importadora.system.services;

import java.util.List;
import org.springframework.stereotype.Service;
import com.importadora.system.model.Producto;
import com.importadora.system.repository.ProductoRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> mostrarCatalogo() {
        return productoRepository.findAll();
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new RuntimeException("No disponible"));
    }

    public void limpiarCatalogoSilencioso() {
        try {
            productoRepository.vaciarYReiniciarIds();
        } catch (Exception e) {
        }
    }
}