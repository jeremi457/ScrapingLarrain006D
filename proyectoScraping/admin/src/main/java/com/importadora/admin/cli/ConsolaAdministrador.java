package com.importadora.admin.cli;

import com.importadora.admin.client.TiendaClient;
import com.importadora.admin.dto.ProductoPublicadoDTO;
import com.importadora.admin.services.ScrapingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class ConsolaAdministrador implements CommandLineRunner {

    private final ScrapingService scrapingService;
    private final TiendaClient tiendaClient;

    public ConsolaAdministrador(ScrapingService scrapingService, TiendaClient tiendaClient) {
        this.scrapingService = scrapingService;
        this.tiendaClient = tiendaClient;
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;
        
        while (!salir) {
            System.out.println("\n=============================================");
            System.out.println(" ⚙️ PANEL DE ADMINISTRACIÓN (SCRAPER HÍBRIDO)");
            System.out.println("=============================================");
            System.out.println("1. Extraer Catálogo por Categoría (Segundo Plano)");
            System.out.println("2. Apagar Panel");
            System.out.println("=============================================");
            System.out.print("👉 Seleccione una opción: ");
            
            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    ejecutarFlujoPorCategoria(scanner);
                    break;
                case "2":
                    System.out.println("\n👋 Apagando...");
                    salir = true;
                    break;
                default:
                    System.out.println("\n❌ Opción no válida.");
            }
        }
        scanner.close();
        System.exit(0);
    }

    private void ejecutarFlujoPorCategoria(Scanner scanner) {
        System.out.println("\n📂 SELECCIONE LA CATEGORÍA A EXPLORAR:");
        System.out.println("1. Hogar Inteligente y Jardín");
        System.out.println("2. Computación y Tablets");
        System.out.println("3. Electrónica de Consumo");
        System.out.println("4. Teléfonos y Accesorios");
        System.out.println("5. Deportes y Exteriores");
        System.out.print("👉 Ingrese el número de la categoría: ");
        
        int catOpcion = leerEntero(scanner);
        String urlObjetivo = obtenerUrlPorCategoria(catOpcion);
        
        if (urlObjetivo == null) {
            System.out.println("❌ Opción de categoría inválida. Cancelando operación.");
            return;
        }

        System.out.print("\n📊 ¿Cuántos productos máximos desea extraer? (ej. 20, 50, 100): ");
        int limite = leerEntero(scanner);
        if (limite <= 0) limite = 20;

        System.out.println("\n⏳ Iniciando extracción en segundo plano (Modo Headless activo)...");
        System.out.println("   (Esto puede tomar unos segundos mientras el bot evade protecciones)");
        
        // Pasamos la URL específica y el límite al servicio
        List<ProductoPublicadoDTO> resultados = scrapingService.ejecutarScrapingHibrido(urlObjetivo, limite);

        if (resultados.isEmpty()) {
            System.out.println("❌ No se extrajeron datos. (Posible bloqueo o página vacía).");
            return;
        }

        System.out.println("\n📦 RESULTADOS ENCONTRADOS:");
        System.out.println("--------------------------------------------------");
        for (int i = 0; i < resultados.size(); i++) {
            ProductoPublicadoDTO p = resultados.get(i);
            System.out.printf("[%d] %s | Costo Base: $%.2f USD\n", (i + 1), p.nombreProducto(), p.precioLocal());
        }
        System.out.println("--------------------------------------------------");

        while (true) {
            System.out.print("\n🚀 Ingrese el ID del producto para ENVIAR A LA TIENDA (o '0' para salir): ");
            int idSeleccionado = leerEntero(scanner);

            if (idSeleccionado == 0) break;

            if (idSeleccionado > 0 && idSeleccionado <= resultados.size()) {
                ProductoPublicadoDTO dtoElegido = resultados.get(idSeleccionado - 1);
                try {
                    tiendaClient.enviarProductoALaTienda(dtoElegido);
                    System.out.println("✅ Producto enviado con éxito.");
                } catch (Exception e) {
                    System.out.println("❌ Error HTTP. ¿Está encendido el microservicio cliente en el puerto 8082?");
                }
            } else {
                System.out.println("⚠️ ID inválido.");
            }
        }
    }
    
    // Método que mapea la opción en español a la URL real de la tienda
    private String obtenerUrlPorCategoria(int opcion) {
        return switch (opcion) {
            case 1 -> "https://www.geekbuying.com/category/Smart-Home-Garden-174";
            case 2 -> "https://www.geekbuying.com/category/Computers-Tablets-Accessories-971";
            case 3 -> "https://www.geekbuying.com/category/Consumer-Electronics-177";
            case 4 -> "https://www.geekbuying.com/category/Phones-Accessories-178";
            case 5 -> "https://www.geekbuying.com/category/Sports-Outdoors-1241";
            default -> null;
        };
    }

    private int leerEntero(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }
}