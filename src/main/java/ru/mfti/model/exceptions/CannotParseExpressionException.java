package ru.mfti.model.exceptions;

import ru.mfti.model.Token;

public class CannotParseExpressionException extends Exception{

    Token token;
    String description;

    public CannotParseExpressionException(Token token, String message){
        super(message);
        this.token = token;
    }
    public CannotParseExpressionException(){}

    public CannotParseExpressionException(String message){
        super(message);
    }

}
