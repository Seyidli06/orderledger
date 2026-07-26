package com.adil.orderledger.exception;

public class ConcurrentStockUpdateException extends RuntimeException {

    public ConcurrentStockUpdateException(String message) {
        super(message);
    }
}