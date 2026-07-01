package com.importadora.system.cli;

import com.importadora.system.model.Pedido;
import com.importadora.system.model.Producto;
import com.importadora.system.repository.PedidoRepository;
import com.importadora.system.repository.ProductoRepository;
import com.importadora.system.services.CatalogoScrapingService;
import com.importadora.system.services.PedidoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class ConsolaCliente implements CommandLineRunner {

    private final ProductoRepository productoRepository;
    private final PedidoService pedidoService;
    private final CatalogoScrapingService catalogoScrapingService;
    private final PedidoRepository pedidoRepository;

    private final List<Producto> carrito = new ArrayList<>();

    public ConsolaCliente(ProductoRepository productoRepository,
                          PedidoService pedidoService,
                          CatalogoScrapingService catalogoScrapingService,
                          PedidoRepository pedidoRepository) {
        this.productoRepository = productoRepository;
        this.pedidoService = pedidoService;
        this.catalogoScrapingService = catalogoScrapingService;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner teclado = new Scanner(System.in);

        System.out.println("\n==================================================");
        System.out.println("🛒  TIENDA DE IMPORTACIONES — PANEL CLIENTE");
        System.out.println("==================================================");

        boolean salir = false;
        while (!salir) {
            System.out.println("\n---------------------------------------------");
            System.out.println("1. Ver catálogo");
            System.out.println("2. Ver carrito");
            System.out.println("3. Finalizar compra");
            System.out.println("4. Ver historial de compras");
            System.out.println("5. Salir");
            System.out.println("---------------------------------------------");
            System.out.print("👉 Seleccione una opción: ");

            switch (teclado.nextLine().trim()) {
                case "1" -> verCatalogo(teclado);
                case "2" -> verCarrito();
                case "3" -> finalizarCompra(teclado);
                case "4" -> verHistorial(teclado);
                case "5" -> {
                    System.out.println("\n👋 Hasta luego.");
                    salir = true;
                }
                default -> System.out.println("❌ Opción no válida. Intente de nuevo.");
            }
        }
        teclado.close();
    }

    // ─────────────────────────────────────────────
    // OPCIÓN 1 — Ver catálogo
    // Promueve automáticamente lo que admin haya dejado en producto_scrapeado,
    // luego muestra el catálogo completo con el precio al público.
    // ─────────────────────────────────────────────
    private void verCatalogo(Scanner teclado) {
        long pendientes = catalogoScrapingService.contarPendientes();
        if (pendientes > 0) {
            System.out.printf("\n📥 Incorporando %d producto(s) nuevo(s) al catálogo...\n", pendientes);
            int promovidos = catalogoScrapingService.promoverProductosPendientes();
            System.out.printf("✅ %d producto(s) publicados.\n", promovidos);
        }

        List<Producto> disponibles = productoRepository.findAll();

        if (disponibles.isEmpty()) {
            System.out.println("\n📦 El catálogo está vacío. El administrador aún no ha publicado productos.");
            return;
        }

        System.out.println("\n🔍 CATÁLOGO DISPONIBLE:");
        System.out.println("─────────────────────────────────────────────────────────────────────");
        System.out.printf("%-6s %-55s %-14s %-8s%n", "ID", "Producto", "Precio (USD)", "Stock");
        System.out.println("─────────────────────────────────────────────────────────────────────");
        for (Producto p : disponibles) {
            String nombre = p.getNombreProducto().length() > 52
                    ? p.getNombreProducto().substring(0, 49) + "..."
                    : p.getNombreProducto();
            System.out.printf("%-6d %-55s $%-13.2f %-8d%n",
                    p.getIdProducto(), nombre, p.getPrecioAlPublico(), p.getStockBodega());
        }
        System.out.println("─────────────────────────────────────────────────────────────────────");

        // Agregar al carrito desde aquí, sin salir de la vista
        while (true) {
            System.out.print("\n➕ Ingrese el ID del producto a agregar al carrito (o 0 para volver): ");
            int id = leerEntero(teclado);
            if (id == 0) break;

            Producto p = productoRepository.findById((long) id).orElse(null);
            if (p == null || p.getStockBodega() <= 0) {
                System.out.println("⚠️  Producto no disponible o sin stock.");
            } else {
                carrito.add(p);
                System.out.printf("✅ '%s' agregado al carrito.\n", p.getNombreProducto());
            }
        }
    }

    // ─────────────────────────────────────────────
    // OPCIÓN 2 — Ver carrito
    // Muestra productos, precio base, IVA (19%), costo de importación (31%) y total.
    // ─────────────────────────────────────────────
    private void verCarrito() {
        if (carrito.isEmpty()) {
            System.out.println("\n🛒 Su carrito está vacío.");
            return;
        }

        System.out.println("\n🛒 CARRITO DE COMPRAS:");
        System.out.println("─────────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-4s %-45s %-12s %-12s %-12s %-12s%n",
                "#", "Producto", "Base (USD)", "Import.31%", "IVA 19%", "Total");
        System.out.println("─────────────────────────────────────────────────────────────────────────────");

        double totalCarrito = 0;
        for (int i = 0; i < carrito.size(); i++) {
            Producto p = carrito.get(i);
            double base       = p.getPrecioLocal();
            double conImport  = base * 1.31;
            double conIva     = conImport * 1.19;
            double importCost = conImport - base;
            double ivaCost    = conIva - conImport;

            String nombre = p.getNombreProducto().length() > 42
                    ? p.getNombreProducto().substring(0, 39) + "..."
                    : p.getNombreProducto();

            System.out.printf("%-4d %-45s $%-11.2f $%-11.2f $%-11.2f $%-11.2f%n",
                    (i + 1), nombre, base, importCost, ivaCost, conIva);
            totalCarrito += conIva;
        }

        System.out.println("─────────────────────────────────────────────────────────────────────────────");
        System.out.printf("💰 TOTAL A PAGAR: $%.2f USD%n", totalCarrito);
    }

    // ─────────────────────────────────────────────
    // OPCIÓN 3 — Finalizar compra
    // ─────────────────────────────────────────────
    private void finalizarCompra(Scanner teclado) {
        if (carrito.isEmpty()) {
            System.out.println("\n⚠️  Su carrito está vacío. Agregue productos antes de pagar.");
            return;
        }

        // Mostrar resumen antes de pedir datos
        verCarrito();

        System.out.println("\n📋 DATOS PARA LA COMPRA:");
        System.out.print("   Nombre completo : ");
        String nombre = teclado.nextLine().trim();

        System.out.print("   RUT (ej: 12345678-9): ");
        String rut = teclado.nextLine().trim();

        System.out.print("   Dirección de entrega: ");
        String direccion = teclado.nextLine().trim();

        System.out.print("   Correo electrónico  : ");
        String email = teclado.nextLine().trim();

        System.out.println("\n💳 MÉTODO DE PAGO:");
        System.out.println("   1. Crédito");
        System.out.println("   2. Débito");
        System.out.print("   👉 Seleccione (1 o 2): ");

        String metodoPago;
        switch (teclado.nextLine().trim()) {
            case "1" -> metodoPago = "Crédito";
            case "2" -> metodoPago = "Débito";
            default  -> {
                System.out.println("❌ Opción inválida. Compra cancelada.");
                return;
            }
        }

        try {
            pedidoService.procesarPago(carrito, nombre, rut, email, direccion, metodoPago);
            System.out.println("\n✅ ¡Compra finalizada con éxito!");
            System.out.printf("   Método de pago: %s | RUT: %s%n", metodoPago, rut);
            System.out.println("   Su pedido ha sido registrado.");
            carrito.clear();
        } catch (Exception e) {
            System.out.println("❌ Error al procesar la compra: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // OPCIÓN 4 — Historial de compras por RUT
    // ─────────────────────────────────────────────
    private void verHistorial(Scanner teclado) {
        System.out.print("\n🔎 Ingrese su RUT para consultar el historial (ej: 12345678-9): ");
        String rut = teclado.nextLine().trim();

        List<Pedido> pedidos = pedidoRepository.findByRut(rut);

        if (pedidos.isEmpty()) {
            System.out.printf("\n📭 No se encontraron compras asociadas al RUT '%s'.%n", rut);
            return;
        }

        System.out.printf("\n📜 HISTORIAL DE COMPRAS — RUT: %s (%d pedido(s))%n", rut, pedidos.size());

        for (Pedido pedido : pedidos) {
            System.out.println("\n══════════════════════════════════════════════");
            System.out.printf("  Pedido #%-6d | %s | Total: $%.2f USD%n",
                    pedido.getIdPedido(), pedido.getMetodoPago(), pedido.getTotalPedido());
            System.out.printf("  Cliente  : %s%n", pedido.getNombreCliente());
            System.out.printf("  Dirección: %s | Email: %s%n", pedido.getDireccion(), pedido.getEmail());
            System.out.println("  Productos:");
            pedido.getItems().forEach(item ->
                System.out.printf("    • %-50s $%.2f USD%n", item.getNombreProducto(), item.getPrecioCobrado())
            );
        }
        System.out.println("══════════════════════════════════════════════");
    }

    private int leerEntero(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }
}