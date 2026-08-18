package org.example.stocktradingplatform.ExceptionManager;

public class ProductNotFind extends RuntimeException {
    public ProductNotFind(String message) {
        super(message);
    }
}
