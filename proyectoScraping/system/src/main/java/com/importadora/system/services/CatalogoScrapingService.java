package com.importadora.system.services;

import com.importadora.system.model.Producto;
import com.importadora.system.model.ProductoScrapeado;
import com.importadora.system.repository.ProductoRepository;
import com.importadora.system.repository.ProductoScrapeadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CatalogoScrapingService {

    private static final double MARKUP = 1.50;
    private static final int STOCK_MINIMO = 5;
    private static final int STOCK_VARIACION = 20;

    private final ProductoScrapeadoRepository productoScrapeadoRepository;
    private final ProductoRepository productoRepository;

    public CatalogoScrapingService(ProductoScrapeadoRepository productoScrapeadoRepository,
                                    ProductoRepository productoRepository) {
        this.productoScrapeadoRepository = productoScrapeadoRepository;
        this.productoRepository = productoRepository;
    }


    public long contarPendientes() {
        return productoScrapeadoRepository.count();
    }


    @Transactional
    public int promoverProductosPendientes() {
        List<ProductoScrapeado> pendientes = productoScrapeadoRepository.findAll();

        if (pendientes.isEmpty()) {
            return 0;
        }

        for (ProductoScrapeado origen : pendientes) {
            Producto nuevo = new Producto();
            nuevo.setNombreProducto(origen.getNombreProducto());
            nuevo.setPrecioLocal(origen.getPrecioLocal()); 
            nuevo.setTipoProducto(origen.getTipoProducto());
            nuevo.setStockBodega((int) (Math.random() * STOCK_VARIACION) + STOCK_MINIMO);

            productoRepository.save(nuevo);
        }

        productoScrapeadoRepository.deleteAll(pendientes);

        return pendientes.size();
    }
}