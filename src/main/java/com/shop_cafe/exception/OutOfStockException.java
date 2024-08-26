package com.shop_cafe.exception;

public class OutOfStockException extends RuntimeException{
    public OutOfStockException(String message){
        super(message);
    }
}
