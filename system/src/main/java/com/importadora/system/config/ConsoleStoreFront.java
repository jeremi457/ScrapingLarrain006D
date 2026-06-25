package com.importadora.system.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.importadora.system.model.Pedido;
import com.importadora.system.model.Producto;
import com.importadora.system.repository.PedidoRepository;
import com.importadora.system.services.ProductoService;
import com.importadora.system.services.ScrapingService;

@Component
public class ConsoleStoreFront implements CommandLineRunner {


    private final ScrapingService scrapingService;
    private final ProductoService productoService;
    private final PedidoRepository pedidoRepository;

    

    public ConsoleStoreFront(ScrapingService scrapingService, ProductoService productoService,
            PedidoRepository pedidoRepository) {
        this.scrapingService = scrapingService;
        this.productoService = productoService;
        this.pedidoRepository = pedidoRepository;
    }

    private final List<Producto> carrito = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) throws Exception {
        boolean salir = false;
        
        while (!salir) {
            System.out.println("\n=============================================");
            System.out.println("        LARRAÍN IMPORTACIONES - CONSOLA      ");
            System.out.println("=============================================");
            System.out.println("1. Ejecutar Escaneo Automático Global y Comprar");
            System.out.println("2. Ver Carrito Actual (" + carrito.size() + " productos)");
            System.out.println("3. Procesar Pago (Checkout)");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");

            int opcion = leerEntero();

            switch (opcion) {
                case 1 -> iniciarBusquedaAutomatica();
                case 2 -> mostrarCarrito();
                case 3 -> procesarCheckout();
                case 4 -> {

                    productoService.limpiarCatalogoSilencioso();
                    

                    salir = true;
                    System.out.println("\nPrograma finalizado con éxito.");
                }
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void iniciarBusquedaAutomatica() {
        System.out.println("\n--- CANTIDAD DE PRODUCTOS A RECOLECTAR ---");
        System.out.println("1. Mostrar 10 Productos");
        System.out.println("2. Mostrar 20 Productos");
        System.out.println("3. Mostrar 30 Productos");
        System.out.print("Seleccione un límite: ");
        int opcionLimite = leerEntero();
        
        int limiteReal = switch (opcionLimite) {
            case 2 -> 20;
            case 3 -> 30;
            default -> 10;
        };

        List<Producto> escaneados = scrapingService.ejecutarScrapingAutomatico(limiteReal);

        if (escaneados.isEmpty()) {
            System.out.println("No se pudieron recuperar productos en este instante. Intente más tarde.");
            return;
        }

        boolean interactuando = true;
        while (interactuando) {
            System.out.println("\n=======================================================================================");
            System.out.printf("%-6s | %-60s | %-15s\n", "ID", "PRODUCTO DETECTADO (LAS 3 URLS COMBINADAS)", "PRECIO (CLP)");
            System.out.println("=======================================================================================");
            for (Producto p : escaneados) {
                System.out.printf("%-6d | %-60.60s | $%,12.0f\n", p.getIdProducto(), p.getNombreProducto(), p.getPrecioLocal());
            }
            System.out.println("=======================================================================================");
            
            System.out.print("\nIngrese el ID del producto para añadir al carrito (0 para volver al menú principal): ");
            long idSel = leerLong();

            if (idSel == 0) {
                interactuando = false;
            } else {
                try {
                    Producto p = productoService.buscarPorId(idSel);
                    carrito.add(p);
                    System.out.println("¡Añadido al carro!");
                } catch (Exception e) {
                    System.out.println("ID inválido. Seleccione únicamente un ID visible en la lista.");
                }
            }
        }
    }

    private void mostrarCarrito() {
        if (carrito.isEmpty()) {
            System.out.println("\n🛒 El carrito está vacío.");
            return;
        }
        System.out.println("\n--- CONTENIDO DEL CARRITO ---");
        double total = 0;
        for (Producto p : carrito) {
            System.out.println("- " + p.getNombreProducto() + " ($" + String.format("%,.0f", p.getPrecioLocal()) + " CLP)");
            total += p.getPrecioLocal();
        }
        System.out.printf("TOTAL ACUMULADO: $%,.0f CLP\n", total);
    }

    private void procesarCheckout() {
        if (carrito.isEmpty()) {
            System.out.println("\nTu carrito está vacío. Agrega productos escaneados primero.");
            return;
        }

        double total = carrito.stream().mapToDouble(Producto::getPrecioLocal).sum();

        System.out.println("\n=============================================");
        System.out.println("          CHECKOUT - DATOS DEL CLIENTE       ");
        System.out.println("=============================================");
        
        System.out.print("Nombre Completo: ");
        String nombre = scanner.nextLine();
        
        System.out.print("RUT: ");
        String rut = scanner.nextLine();
        
        System.out.print("Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Dirección de Envío: ");
        String direccion = scanner.nextLine();
        
        System.out.print("Método de Pago: ");
        String metodoPago = scanner.nextLine();

        Pedido pedido = new Pedido();
        pedido.setNombreCliente(nombre);
        pedido.setRut(rut);
        pedido.setEmail(email);
        pedido.setDireccion(direccion);
        pedido.setMetodoPago(metodoPago);
        pedido.setTotalPedido(total);

        pedidoRepository.save(pedido);

        System.out.println("\n=============================================");
        System.out.println("¡ORDEN PROCESADA Y ASENTADA EN BASE DE DATOS!");
        System.out.println("=============================================");
        System.out.printf("Cliente: %s | Total Pagado: $%,.0f CLP\n", nombre, total);
        System.out.println("=============================================");
        
        carrito.clear(); 
    }

    private int leerEntero() {
        try {
            int num = scanner.nextInt();
            scanner.nextLine(); 
            return num;
        } catch (Exception e) {
            scanner.nextLine();
            return -1;
        }
    }

    private long leerLong() {
        try {
            long num = scanner.nextLong();
            scanner.nextLine(); 
            return num;
        } catch (Exception e) {
            scanner.nextLine();
            return -1;
        }
    }
}