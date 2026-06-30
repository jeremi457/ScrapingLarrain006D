package com.importadora.system.cli;

import com.importadora.system.model.Producto;
import com.importadora.system.repository.ProductoRepository;
import com.importadora.system.services.PedidoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class ConsolaCliente implements CommandLineRunner {

    private final ProductoRepository productoRepository;
    private final PedidoService pedidoService; // 🚀 Inyectamos el nuevo servicio

    public ConsolaCliente(ProductoRepository productoRepository, PedidoService pedidoService) {
        this.productoRepository = productoRepository;
        this.pedidoService = pedidoService;
    }

    private final List<Producto> carrito = new ArrayList<>();

    @Override
    public void run(String... args) throws Exception {
        Scanner teclado = new Scanner(System.in);
        
        System.out.println("\n==================================================");
        System.out.println("🛒 TIENDA DE IMPORTACIONES (COMPRADOR FINAL)");
        System.out.println("==================================================");

        while (true) {
            System.out.print("\n🛍️ [Cliente] Menú: (catalogo / agregar [ID] / pagar / salir): ");
            String entrada = teclado.nextLine().trim().toLowerCase();

            if (entrada.equals("salir")) {
                System.out.println("👋 Saliendo de la tienda del comprador...");
                break;
            }

            if (entrada.equals("catalogo")) {
                List<Producto> disponibles = productoRepository.findAll();
                if (disponibles.isEmpty()) {
                    System.out.println("📦 Catálogo vacío. El administrador aún no ha publicado productos.");
                } else {
                    System.out.println("\n🔍 PRODUCTOS DISPONIBLES EN STOCK:");
                    for (Producto p : disponibles) {
                        System.out.printf("  ID: %d | %s | Precio: $%.2f USD | Stock: %d\n", 
                            p.getIdProducto(), p.getNombreProducto(), p.getPrecioAlPublico(), p.getStockBodega());
                    }
                }
                continue;
            }

            if (entrada.startsWith("agregar ")) {
                try {
                    Long idProducto = Long.parseLong(entrada.replace("agregar ", "").trim());
                    Producto p = productoRepository.findById(idProducto).orElse(null);
                    
                    if (p != null && p.getStockBodega() > 0) {
                        carrito.add(p);
                        System.out.println("✅ Añadido: " + p.getNombreProducto() + " ($" + p.getPrecioAlPublico() + " USD)");
                    } else {
                        System.out.println("⚠️ Producto no disponible o sin existencias.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Use el formato: agregar [ID]");
                }
                continue;
            }

            if (entrada.equals("pagar")) {
                if (carrito.isEmpty()) {
                    System.out.println("⚠️ Su carrito está vacío.");
                    continue;
                }

                System.out.println("\n🧾 RESUMEN DEL PEDIDO:");
                double totalSimulado = 0;
                for (Producto p : carrito) {
                    System.out.printf("  • %s -> $%.2f USD\n", p.getNombreProducto(), p.getPrecioAlPublico());
                    totalSimulado += p.getPrecioAlPublico();
                }
                System.out.printf("💰 TOTAL A PAGAR: $%.2f USD\n", totalSimulado);

                System.out.print("📝 Ingrese su nombre: ");
                String nombre = teclado.nextLine();
                System.out.print("📝 Ingrese su RUT: ");
                String rut = teclado.nextLine();
                
                try {
                    // Delegamos todo el trabajo pesado a la capa de Servicio
                    pedidoService.procesarPago(carrito, nombre, rut);
                    System.out.println("✅ ¡Compra finalizada! Su pedido ha sido guardado en la base de datos.");
                    carrito.clear();
                } catch (Exception e) {
                    System.out.println("❌ Error al procesar el pago: " + e.getMessage());
                }
                continue;
            }
        }
        teclado.close();
    }
}