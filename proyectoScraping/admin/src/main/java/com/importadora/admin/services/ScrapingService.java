package com.importadora.admin.services;

import com.importadora.admin.dto.ProductoPublicadoDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsabilidad única: scrapear y devolver resultados en memoria.
 * No decide qué se guarda ni qué se publica — esa decisión la toma
 * el administrador desde la consola.
 */
@Service
public class ScrapingService {

    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
    };

    private final Random random = new Random();

    // Ya no depende de ningún repositorio: scrapea y devuelve, nada más.
    public List<ProductoPublicadoDTO> ejecutarScrapingHibrido(String urlCategoria, int limiteTotal) {
        List<ProductoPublicadoDTO> productosExtraidos = new ArrayList<>();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        String uaAleatorio = USER_AGENTS[random.nextInt(USER_AGENTS.length)];
        options.addArguments("user-agent=" + uaAleatorio);
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = new ChromeDriver(options);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            int paginaActual = 1;

            while (productosExtraidos.size() < limiteTotal) {

                String urlConPagina = urlCategoria + "?page=" + paginaActual;
                System.out.printf("   🌐 Navegando invisible a Página %d... (Progreso: %d/%d productos)\n",
                        paginaActual, productosExtraidos.size(), limiteTotal);

                driver.get(urlConPagina);
                Thread.sleep(3500);

                for (int i = 0; i < 5; i++) {
                    js.executeScript("window.scrollBy(0, 900)");
                    esperarAleatorio(800, 1500);
                }

                String htmlCrudo = driver.getPageSource();
                Document doc = Jsoup.parse(htmlCrudo);

                Elements cards = doc.select("li[class*='_li'], div[class*='product'], div[class*='item'], .search_li, .category_li, li.item");

                int productosNuevosEnEstaPagina = 0;

                for (Element card : cards) {
                    if (productosExtraidos.size() >= limiteTotal) break;

                    String nombre = card.select("[class*='name'], .name, a.title, .pro_name").text().trim();
                    String precioTexto = card.select("[class*='price'], .price, .pro_price").text().trim();

                    if (nombre.isEmpty() || precioTexto.isEmpty() || nombre.length() < 10) continue;

                    double precioUSD = extraerPrecio(precioTexto);
                    if (precioUSD <= 0) continue;

                    String nombreFinal = nombre.length() > 240 ? nombre.substring(0, 235) + "..." : nombre;
                    boolean yaExiste = productosExtraidos.stream()
                            .anyMatch(p -> p.nombreProducto().equalsIgnoreCase(nombreFinal));

                    if (!yaExiste) {
                        productosExtraidos.add(new ProductoPublicadoDTO(
                                nombreFinal,
                                precioUSD,
                                "Importación por Categoría"
                        ));
                        productosNuevosEnEstaPagina++;
                    }
                }

                System.out.printf("   ✨ Página %d analizada. Se capturaron %d productos válidos.\n",
                        paginaActual, productosNuevosEnEstaPagina);

                if (productosNuevosEnEstaPagina == 0) {
                    System.out.println("   ⚠️ No se detectaron productos nuevos en esta página. Finalizando recolección.");
                    break;
                }

                paginaActual++;
                if (paginaActual > 15) break;
            }

        } catch (Exception e) {
            System.out.println("   [!] Error inesperado en el flujo de backend: " + e.getMessage());
        } finally {
            driver.quit();
        }

        return productosExtraidos;
    }

    private void esperarAleatorio(int minMs, int maxMs) throws InterruptedException {
        Thread.sleep(random.nextInt((maxMs - minMs) + 1) + minMs);
    }

    private double extraerPrecio(String texto) {
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(texto.replace(",", ""));
        if (matcher.find()) return Double.parseDouble(matcher.group());
        return 0.0;
    }
}