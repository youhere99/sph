package com.zmx.mitm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionCatch {
 
    @ExceptionHandler(Exception.class)
    public void customException(Exception e) {
        e.printStackTrace();
    }
 
}