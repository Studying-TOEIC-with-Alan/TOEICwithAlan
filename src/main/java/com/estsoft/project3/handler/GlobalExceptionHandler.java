package com.estsoft.project3.handler;

import com.estsoft.project3.dto.ErrorHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorHandler> handleRuntimeException(RuntimeException ex) {
        ErrorHandler error = new ErrorHandler("404", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}
