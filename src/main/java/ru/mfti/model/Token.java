package ru.mfti.model;

import ru.mfti.model.exceptions.CannotParseExpressionException;
import ru.mfti.model.util.ConstantsUtil;
import ru.mfti.model.util.ExpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Token {


    public final Type type;
    private final String string;

    // If token is a function:
    private String envelopingFunction;
    private List<Token> argumentTokens;

    public Token(String string, Type type) throws CannotParseExpressionException {
        this.type = type;
        this.string = ExpUtil.trimBrackets(string);
        parseEnvelopingFunction();
        if(string.isEmpty()) throw new CannotParseExpressionException();
    }

    private void parseEnvelopingFunction() throws CannotParseExpressionException {
        Optional<String> function = ExpUtil.getEnvelopingFunctionName(this.string);
        if (function.isPresent()) {
            envelopingFunction = function.get();
            argumentTokens = splitArgumentTokens(ExpUtil.stripEnvelopingFunction(this.string));
        } else if (this.string.contains(","))
            throw new CannotParseExpressionException("Misplaced ',' symbol encountered!");
    }

    private List<Token> splitArgumentTokens(String expInBrackets) throws CannotParseExpressionException {
        List<Token> args = new ArrayList<>();
        for (String arg : expInBrackets.split(",")) {
            if (arg.isEmpty()) continue;
            Token argToken = new Token(arg, Token.Type.COMPLEX);
            args.add(argToken);
        }
        return args;
    }

    public double toDouble() throws CannotParseExpressionException {
        try {
            double multiplier = 1;
            String trimmedString = string;

            // check for leading number with reduced multiplication sign like 10pi
            Optional<String> leadingNumber = ExpUtil.getLongestNumberSubstring(string, 0);
            if (leadingNumber.isPresent() && leadingNumber.get().length() < string.length()) {
                multiplier = Double.parseDouble(leadingNumber.get());
                trimmedString = string.substring(leadingNumber.get().length());
            }

            // Check for a chain of constants like (e, pi, epi)
            Optional<Double> optional = ConstantsUtil.parseConstants(trimmedString.toLowerCase());

            if (optional.isPresent()) return optional.get() * multiplier;
            return Double.parseDouble(trimmedString) * multiplier;
        } catch (Exception e) {
            throw new CannotParseExpressionException(this, "Cannot parse value '" + string + "' to a number!");
        }
    }


    public int toInt() throws CannotParseExpressionException {
        return (int) (toDouble());
    }

    public String getString() {
        return string;
    }

    public Type getType() {
        return type;
    }

    public String getEnvelopingFunction() {
        return envelopingFunction;
    }

    public List<Token> getArgumentTokens() {
        return argumentTokens;

    }

    @Override
    public String toString() {
        return this.string;
    }


    public enum Type {
        OPERATOR,
        VALUE,
        COMPLEX
    }
}
