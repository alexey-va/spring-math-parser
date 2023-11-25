package ru.mfti.model;

import ru.mfti.model.exceptions.CannotParseExpressionException;
import ru.mfti.model.util.ConstantsUtil;
import ru.mfti.model.util.ExpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Token {


    public final Type type;
    private String string;
    Token parent;

    // If token is a function:
    private String envelopingFunction;
    private List<Token> argumentTokens;

    public Token(String string, Type type) throws CannotParseExpressionException {
        //System.out.println("nt: "+string);
        this.type = type;
        this.string = ExpUtil.trimBrackets(string);
        //System.out.println("nb: "+this.string);
        //System.out.println("created token: "+string+" "+type+" "+this.string);
        parseEnvelopingFunction();
    }

    public Token(String string, Type type, Token parent) throws CannotParseExpressionException {
        this.type = type;
        this.string = ExpUtil.trimBrackets(string)
                .replaceFirst("^0+(?!$)", "")
                .replaceFirst("^[+]+(?!$)", "");
        this.parent = parent;
        //System.out.println("created token: "+string+" "+type+" "+this.string);
        parseEnvelopingFunction();
    }

    private void parseEnvelopingFunction() throws CannotParseExpressionException {
        Optional<String> function = ExpUtil.getEnvelopingFunctionName(this.string);
        if (function.isPresent()) {
            envelopingFunction = function.get();
            argumentTokens = splitArgumentTokens(ExpUtil.stripEnvelopingFunction(this.string));
        } else if (this.string.contains(",")) {
            throw new CannotParseExpressionException();
        }
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

            // check for smth like 10pi
            Optional<String> leadingNumber = ExpUtil.getLongestNumberSubstring(string, 0);
            if (leadingNumber.isPresent() && leadingNumber.get().length() < string.length()) {
                multiplier = Double.parseDouble(leadingNumber.get());
                trimmedString = string.substring(leadingNumber.get().length());
            }

            // check if it is a named constant
            Optional<Double> optional = ConstantsUtil.parseConstants(trimmedString.toLowerCase());

            if (optional.isPresent()) return optional.get() * multiplier;
            return Double.parseDouble(trimmedString) * multiplier;
        } catch (Exception e) {
            e.printStackTrace();
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

    public Token getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return this.string;
    }

    public void setParent(Token token) {
        this.parent = token;
    }


    public enum Type {
        OPERATOR,
        VALUE,
        COMPLEX
    }
}
