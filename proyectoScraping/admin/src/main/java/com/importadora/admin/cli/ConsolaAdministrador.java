package com.importadora.admin.cli;

import com.importadora.admin.client.TiendaClient;
import com.importadora.admin.dto.ProductoPublicadoDTO;
import com.importadora.admin.services.ScrapingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    private List<ProductoPublicadoDTO> ultimaBusqueda = new ArrayList<>();

    @Override
    public void run(String... args) throws Exception {
        Scanner teclado = new Scanner(System.in);
        
        System.out.println("\n==================================================");
        System.out.println("🛡️ PANEL DEL ADMINISTRADOR (IMPORTADORA SYSTEM)");
        System.out.println("==================================================");

        while (true) {
            System.out.print("\n🛠️ [Admin] Buscar producto o comando (publicar [N°] / salir): ");
            String entrada = teclado.nextLine().trim();

            if (entrada.equalsIgnoreCase("salir")) {
                System.out.println("👋 Cerrando sesión del administrador...");
                break;
            }

            if (entrada.toLowerCase().startsWith("publicar ")) {
                if (ultimaBusqueda.isEmpty()) {
                    System.out.println("⚠️ Realiza una búsqueda antes de publicar.");
                    continue;
                }
                try {
                    int index = Integer.parseInt(entrada.replace("publicar ", "").trim()) - 1;
                    if (index >= 0 && index < ultimaBusqueda.size()) {
                        ProductoPublicadoDTO seleccionado = ultimaBusqueda.get(index);
                        
                        // Enviar por HTTP REST al Microservicio del Cliente
                        tiendaClient.enviarProductoALaTienda(seleccionado);
                        
                        System.out.println("✅ ¡Producto enviado a la tienda!");
                        System.out.printf("   📦 %s\n", seleccionado.nombreProducto());
                        System.out.printf("   💵 Costo Base: $%.2f USD | Precio Cliente (+50%%): $%.2f USD\n", 
                                seleccionado.precioLocal(), (seleccionado.precioLocal() * 1.50));
                    } else {
                        System.out.println("⚠️ Número fuera de rango.");
                    }
                } catch (Exception e) {
                    System.out.println("❌ No se pudo publicar en el canal REST: " + e.getMessage());
                }
                continue;
            }

            if (!entrada.isEmpty()) {
                System.out.println("⏳ Extrayendo opciones en tiempo real desde China...");
                ultimaBusqueda = scrapingService.buscarYScrapear(entrada);

                if (ultimaBusqueda.isEmpty()) {
                    System.out.println("❌ No se encontraron resultados exactos.");
                } else {
                    System.out.println("\n🔍 PRODUCTOS DISPONIBLES EN PROVEEDOR:");
                    for (int i = 0; i < ultimaBusqueda.size(); i++) {
                        ProductoPublicadoDTO p = ultimaBusqueda.get(i);
                        System.out.printf("  %d. %s | Costo Base: $%.2f USD\n", i + 1, p.nombreProducto(), p.precioLocal());
                    }
                    System.out.println("\n💡 Escribe 'publicar [número]' para enviarlo al catálogo del cliente.");
                }
            }
        }
        teclado.close();
    }
}
