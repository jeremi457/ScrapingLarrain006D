package com.importadora.system.dto;

public record ProductoPublicadoDTO(
    String nombreProducto,
    Double precioLocal,
    String tipoProducto
) {}