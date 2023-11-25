package ru.mfti.model.arithmetics.functions;

import ru.mfti.model.Token;
import ru.mfti.model.exceptions.CannotParseExpressionException;

import java.util.List;

@FunctionalInterface
public interface Computable {

    Token compute(List<Token> args) throws CannotParseExpressionException;

}
