package com.linktic.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(int requested, int available) {
        super(String.format("Stock insuficiente. Solicitado: %d, Disponible: %d", requested, available));
    }
}
