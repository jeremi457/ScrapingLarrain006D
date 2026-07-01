package com.importadora.admin.cli;

import com.importadora.admin.dto.ProductoPublicadoDTO;
import com.importadora.admin.model.PedidoItemMetrica;
import com.importadora.admin.model.PedidoMetrica;
import com.importadora.admin.model.ProductoScrapeado;
import com.importadora.admin.repository.PedidoMetricaRepository;
import com.importadora.admin.repository.ProductoScrapeadoRepository;
import com.importadora.admin.services.ScrapingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConsolaAdministrador implements CommandLineRunner {

    private final ScrapingService scrapingService;
    private final ProductoScrapeadoRepository productoScrapeadoRepository;
    private final PedidoMetricaRepository pedidoMetricaRepository;

    public ConsolaAdministrador(ScrapingService scrapingService,
                                 ProductoScrapeadoRepository productoScrapeadoRepository,
                                 PedidoMetricaRepository pedidoMetricaRepository) {
        this.scrapingService = scrapingService;
        this.productoScrapeadoRepository = productoScrapeadoRepository;
        this.pedidoMetricaRepository = pedidoMetricaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n=============================================");
            System.out.println("    PANEL DE ADMINISTRACIÓN");
            System.out.println("=============================================");
            System.out.println("1. Extraer catálogo por categoría");
            System.out.println("2. Ver métricas de ventas");
            System.out.println("3. Apagar panel");
            System.out.println("=============================================");
            System.out.print("  Seleccione una opción: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> ejecutarFlujoPorCategoria(scanner);
                case "2" -> verMetricas();
                case "3" -> {
                    System.out.println("\n  Apagando...");
                    salir = true;
                }
                default -> System.out.println("\n  Opción no válida.");
            }
        }
        scanner.close();
        System.exit(0);
    }


    private void ejecutarFlujoPorCategoria(Scanner scanner) {
        System.out.println("\n  SELECCIONE LA CATEGORÍA A EXPLORAR:");
        System.out.println("1. Hogar Inteligente y Jardín");
        System.out.println("2. Computación y Tablets");
        System.out.println("3. Electrónica de Consumo");
        System.out.println("4. Teléfonos y Accesorios");
        System.out.println("5. Deportes y Exteriores");
        System.out.print("  Ingrese el número de la categoría: ");

        String urlObjetivo = obtenerUrlPorCategoria(leerEntero(scanner));
        if (urlObjetivo == null) {
            System.out.println("  Opción de categoría inválida. Cancelando operación.");
            return;
        }

        System.out.print("\n  ¿Cuántos productos máximos desea extraer? (ej. 20, 50, 100): ");
        int limite = leerEntero(scanner);
        if (limite <= 0) limite = 20;

        System.out.println("\n  Iniciando extracción en segundo plano (Modo Headless activo)...");

        List<ProductoPublicadoDTO> resultados = scrapingService.ejecutarScrapingHibrido(urlObjetivo, limite);

        if (resultados.isEmpty()) {
            System.out.println("  No se extrajeron datos. (Posible bloqueo o página vacía).");
            return;
        }

        System.out.println("\n  PRODUCTOS ENCONTRADOS (aún no publicados):");
        System.out.println("─────────────────────────────────────────────────────────");
        for (int i = 0; i < resultados.size(); i++) {
            ProductoPublicadoDTO p = resultados.get(i);
            System.out.printf("[%d] %-50s | $%.2f USD%n",
                    (i + 1),
                    p.nombreProducto().length() > 48
                            ? p.nombreProducto().substring(0, 45) + "..."
                            : p.nombreProducto(),
                    p.precioLocal());
        }
        System.out.println("─────────────────────────────────────────────────────────");

        seleccionarYPublicar(scanner, resultados);
    }

    private void seleccionarYPublicar(Scanner scanner, List<ProductoPublicadoDTO> resultados) {
        System.out.println("\n  ¿Qué desea publicar para el cliente?");
        System.out.println("   • IDs separados por coma (ej: 1,3,5)");
        System.out.println("   • 'todos' para publicar todo el listado");
        System.out.println("   • '0' para descartar y volver al menú");
        System.out.print("  Su selección: ");

        String entrada = scanner.nextLine().trim().toLowerCase();

        if (entrada.equals("0")) {
            System.out.println("🗑️  Descartado. Ningún producto fue publicado.");
            return;
        }

        List<ProductoPublicadoDTO> aGuardar = new ArrayList<>();

        if (entrada.equals("todos")) {
            aGuardar.addAll(resultados);
        } else {
            for (String parte : entrada.split(",")) {
                try {
                    int id = Integer.parseInt(parte.trim());
                    if (id >= 1 && id <= resultados.size()) {
                        aGuardar.add(resultados.get(id - 1));
                    } else {
                        System.out.printf("   ID [%d] fuera de rango, ignorado.%n", id);
                    }
                } catch (NumberFormatException e) {
                    System.out.printf("   '%s' no es un ID válido, ignorado.%n", parte.trim());
                }
            }
        }

        if (aGuardar.isEmpty()) {
            System.out.println("   No se seleccionó ningún producto válido. Nada fue publicado.");
            return;
        }

        int guardados = 0;
        for (ProductoPublicadoDTO dto : aGuardar) {
            try {
                productoScrapeadoRepository.save(new ProductoScrapeado(
                        dto.nombreProducto(), dto.precioLocal(), dto.tipoProducto()
                ));
                guardados++;
            } catch (Exception e) {
                System.out.printf("  No se pudo guardar '%s': %s%n", dto.nombreProducto(), e.getMessage());
            }
        }

        System.out.printf("\n  %d producto(s) publicados en la base de datos.%n", guardados);
        System.out.println("   El cliente los verá al revisar su catálogo.");
    }


    private void verMetricas() {
        List<PedidoMetrica> pedidos = pedidoMetricaRepository.findAllByOrderByRutAscIdPedidoAsc();

        if (pedidos.isEmpty()) {
            System.out.println("\n  Aún no se han registrado compras en la tienda.");
            return;
        }

        Map<String, List<PedidoMetrica>> porCliente = new LinkedHashMap<>();
        for (PedidoMetrica p : pedidos) {
            porCliente.computeIfAbsent(p.getRut(), k -> new ArrayList<>()).add(p);
        }

        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║               MÉTRICAS DE VENTAS — RESUMEN           ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        double granTotal = 0;
        int totalPedidos = 0;

        for (Map.Entry<String, List<PedidoMetrica>> entry : porCliente.entrySet()) {
            String rut = entry.getKey();
            List<PedidoMetrica> pedidosCliente = entry.getValue();
            PedidoMetrica primero = pedidosCliente.get(0);

            double totalCliente = pedidosCliente.stream()
                    .mapToDouble(PedidoMetrica::getTotalPedido).sum();

            System.out.printf("%n  👤 %s  (RUT: %s)  —  %d pedido(s)  |  Total gastado: $%.2f USD%n",
                    primero.getNombreCliente(), rut, pedidosCliente.size(), totalCliente);
            System.out.println("  ──────────────────────────────────────────────────────");

            for (PedidoMetrica pedido : pedidosCliente) {
                System.out.printf("    Pedido #%-4d [%-7s]  $%.2f USD  (%d ítem(s))%n",
                        pedido.getIdPedido(),
                        pedido.getMetodoPago(),
                        pedido.getTotalPedido(),
                        pedido.getItems().size());

                for (PedidoItemMetrica item : pedido.getItems()) {
                    String nombre = item.getNombreProducto().length() > 45
                            ? item.getNombreProducto().substring(0, 42) + "..."
                            : item.getNombreProducto();
                    System.out.printf("       • %-45s  $%.2f USD%n", nombre, item.getPrecioCobrado());
                }
            }

            granTotal += totalCliente;
            totalPedidos += pedidosCliente.size();
        }

        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.printf( "║  Clientes únicos : %-4d                               ║%n", porCliente.size());
        System.out.printf( "║  Total pedidos   : %-4d                               ║%n", totalPedidos);
        System.out.printf( "║  Recaudado total : $%-8.2f USD                    ║%n", granTotal);
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

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