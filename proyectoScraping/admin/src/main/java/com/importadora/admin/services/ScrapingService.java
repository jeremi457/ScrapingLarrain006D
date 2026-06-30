package com.importadora.admin.services;

import com.importadora.admin.dto.ProductoPublicadoDTO;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScrapingService {

    public List<ProductoPublicadoDTO> buscarYScrapear(String palabraClave) {
        String urlBusqueda = "https://www.geekbuying.com/search?keyword=" + palabraClave.replace(" ", "+");
        List<ProductoPublicadoDTO> productosEncontrados = new ArrayList<>();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); 
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(urlBusqueda);
            Thread.sleep(4000); 

            // Evasión de detección antibot nativa mediante JS
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            List<WebElement> contenedores = driver.findElements(By.cssSelector(".search_li"));

            for (WebElement container : contenedores) {
                String nombreTexto = container.findElement(By.cssSelector(".name")).getText();
                String precioTexto = container.findElement(By.cssSelector(".price")).getText();

                String nombreLimpio = nombreTexto.trim();
                if (!nombreLimpio.toLowerCase().contains(palabraClave.toLowerCase())) {
                    continue;
                }

                Pattern pattern = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)");
                Matcher matcher = pattern.matcher(precioTexto.replace(",", ""));
                
                if (matcher.find()) {
                    Double precioUSD = Double.parseDouble(matcher.group(1));

                    // Mapeo directo al DTO decoupled (Cero entidades JPA aquí)
                    ProductoPublicadoDTO producto = new ProductoPublicadoDTO(
                        nombreLimpio,
                        precioUSD,
                        palabraClave.toUpperCase()
                    );
                    
                    productosEncontrados.add(producto);
                }
                if (productosEncontrados.size() >= 5) break; 
            }
        } catch (Exception e) {
             System.out.println("   [Error en Extracción] " + e.getMessage());
        } finally {
            driver.quit(); 
        }

        return productosEncontrados;
    }
}