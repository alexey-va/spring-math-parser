package ru.mfti.model.exceptions;

public class CannotAddFunctionException extends RuntimeException{

    public CannotAddFunctionException() {
    }

    public CannotAddFunctionException(String message) {
        super(message);
    }
}
