package com.importadora.admin.client;

import com.importadora.admin.dto.ProductoPublicadoDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TiendaClient {

    private final WebClient webClient;

    public TiendaClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void enviarProductoALaTienda(ProductoPublicadoDTO dto) {
        webClient.post()
                .uri("/productos")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(Void.class)
                .block(); 
    }
}