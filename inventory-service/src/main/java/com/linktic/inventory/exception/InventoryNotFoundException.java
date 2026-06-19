package com.linktic.inventory.exception;

import java.util.UUID;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(UUID productoId) {
        super("Inventario no encontrado para el producto: " + productoId);
    }
}
