package ru.mfti.model.arithmetics;

import ru.mfti.model.Token;
import ru.mfti.model.arithmetics.functions.BinaryOperator;
import ru.mfti.model.arithmetics.functions.CalcFunction;
import ru.mfti.model.arithmetics.functions.Computable;
import ru.mfti.model.arithmetics.functions.UnaryOperator;
import ru.mfti.model.exceptions.CannotParseExpressionException;
import ru.mfti.model.util.ExpUtil;

import java.util.*;

public abstract class ArithmeticProvider {


    protected final Map<String, CalcFunction> functionMap = new HashMap<>();


    public boolean isOperatorName(String name) {
        CalcFunction function = functionMap.get(name);
        return function instanceof UnaryOperator || function instanceof BinaryOperator;
    }

    public int getOperatorPriority(Token operatorToken) {
        return functionMap.get(operatorToken.getString()).getPriority();
    }

    public boolean isBinaryOperator(Token operator) {
        return functionMap.get(operator.getString()) instanceof BinaryOperator;
    }

    public boolean isUnaryOperator(Token operator) {
        return functionMap.get(operator.getString()) instanceof UnaryOperator;
    }

    public boolean isPrefixOperator(Token operator) {
        if (functionMap.get(operator.toString()) instanceof UnaryOperator unaryOperator)
            return !unaryOperator.isPostfix();
        return false;
    }

    public boolean isPostfixOperator(Token operator) {
        return !isPrefixOperator(operator);
    }

    public abstract Token multiply(Token token1, Token token2) throws CannotParseExpressionException;

    public abstract List<Token> sanitize(List<Token> tokens) throws CannotParseExpressionException;


    public void addFunction(CalcFunction function) {
        Set<String> aliases = new HashSet<>(function.getAliases());
        aliases.add(function.getName());
        aliases.stream().filter(functionMap::containsKey)
                .findAny()
                .ifPresentOrElse(s -> System.out.println(s + " is already taken by function " + functionMap.get(s)),
                        () -> aliases.forEach(a -> functionMap.put(a, function)));
    }

    public Token applyFunction(Token token, List<Token> args) throws CannotParseExpressionException {
        System.out.println(token+" "+args);
        if (token.type == Token.Type.COMPLEX) {
            String functionName = token.getEnvelopingFunction();
            if (functionName.isEmpty()) System.out.println("Funcition name is empty!");

            // if it starts with number (like 2sin or -sin)
            Optional<String> leadingNumber = ExpUtil.getLongestNumberSubstring(functionName, 0);
            if (leadingNumber.isEmpty() && functionName.startsWith("-")) leadingNumber = Optional.of("-1");
            if (leadingNumber.isEmpty() && functionName.startsWith("+")) leadingNumber = Optional.of("1");
            if (leadingNumber.isPresent()) functionName = functionName.substring(leadingNumber.get().length());
            //System.out.println("Leading: " + leadingNumber + " name: " + functionName);

            System.out.println("num:" + leadingNumber + " " + functionName + " " + args);

            CalcFunction function = functionMap.get(functionName);

            System.out.println("f: "+function+" lead:"+leadingNumber+" args:"+args);
            if (function == null && args.size() == 1) {
                if(leadingNumber.isEmpty()) leadingNumber = Optional.of(token.getEnvelopingFunction());
                return multiply(new Token(leadingNumber.get(), Token.Type.VALUE), args.get(0));
            }

            // if no such function
            if (function == null)
                throw new CannotParseExpressionException(token, "Cannot find function name '" + token + "'");

            // claculate result and multiply by leading value if present
            Token result = function.compute(args);
            if (leadingNumber.isPresent())
                result = multiply(result, new Token(leadingNumber.get(), Token.Type.VALUE));

            return result;

        } else if (token.type == Token.Type.OPERATOR) {
            String operatorName = token.getString();
            if (operatorName.isEmpty()) System.out.println("Operator name is empty!");

            CalcFunction function = functionMap.get(operatorName);
            if (function == null)
                throw new CannotParseExpressionException(token, "Cannot find function name '" + token + "'");

            return function.compute(args);
        }
        return null;
    }

}
