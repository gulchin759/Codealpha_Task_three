package org.example.stocktradingplatform.ExceptionManager;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ProductNotFind.class, UserrNotFind.class})
    public ResponseEntity<String> handleRuntimeException(RuntimeException  ex) {
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }
}
