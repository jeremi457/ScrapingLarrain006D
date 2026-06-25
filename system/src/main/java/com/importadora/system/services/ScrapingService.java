package com.importadora.system.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.importadora.system.model.Producto;
import com.importadora.system.repository.ProductoRepository;

@Service
public class ScrapingService {


    private final ProductoRepository productoRepository;

    
    public ScrapingService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }


    private static final double TASA_DOLAR_CLP = 940.0;

    private static final String[] URLS_GLOBALES = {
        "https://www.geekbuying.com/bestselling?t=4",
        "https://www.geekbuying.com/warehouse/eu",
        "https://www.geekbuying.com/warehouse/us"
    };

    public List<Producto> ejecutarScrapingAutomatico(int limite) {
        List<Producto> productosNuevos = new ArrayList<>();
        int contador = 0;

        try {
            productoRepository.deleteAll();
        } catch (Exception e) {
            System.out.println("Nota: No se pudo limpiar la base de datos local.");
        }

        System.out.println("\nIniciando barrido automático en las 3 plataformas internacionales...");

        for (String url : URLS_GLOBALES) {
            if (contador >= limite) break;

            try {
                System.out.println("Extrayendo datos desde: " + url);
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(10000)
                        .get();

                Elements items = doc.select(".wh_pro_item, .search_li, .pro_relate_item, li, div");

                for (Element item : items) {
                    if (contador >= limite) break;

                    Element imgTag = item.selectFirst("img");
                    String nombre = (imgTag != null) ? imgTag.attr("alt").trim() : "";
                    if (nombre.isEmpty()) {
                        Element titleTag = item.selectFirst(".wh_pro_name, .pro_name, a");
                        if (titleTag != null) nombre = titleTag.text().trim();
                    }

                    Element priceTag = item.selectFirst(".wh_pro_price, .pro_price, .price");
                    if (priceTag == null || nombre.isEmpty() || nombre.length() < 12) continue;

                    double precioUSD = extraerPrecio(priceTag.text());
                    if (precioUSD <= 0 || precioUSD > 8000) continue;

                    Producto p = new Producto();
                    p.setNombreProducto(nombre.length() > 240 ? nombre.substring(0, 235) + "..." : nombre);
                    p.setPrecioLocal(precioUSD * TASA_DOLAR_CLP);
                    p.setTipoProducto("Importación Directa");
                    p.setStockBodega((int) (Math.random() * 15) + 5);

                    String nombreFinal = p.getNombreProducto();
                    if (productosNuevos.stream().noneMatch(x -> x.getNombreProducto().equalsIgnoreCase(nombreFinal))) {
                        productosNuevos.add(p);
                        contador++;
                    }
                }
            } catch (Exception e) {
                System.out.println("Saltando ruta externa por error de respuesta de red.");
            }
        }

        if (!productosNuevos.isEmpty()) {
            return productoRepository.saveAll(productosNuevos);
        }
        
        return productosNuevos;
    }

    private double extraerPrecio(String texto) {
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(texto.replace(",", ""));
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        }
        return 0.0;
    }
}