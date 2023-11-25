package ru.mfti.model.arithmetics;

import org.springframework.stereotype.Component;
import ru.mfti.model.Token;
import ru.mfti.model.arithmetics.functions.*;
import ru.mfti.model.exceptions.CannotParseExpressionException;
import ru.mfti.model.util.ConstantsUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class BasicArithmeticProvider extends ArithmeticProvider {


    DecimalFormat decimalFormat;
    DecimalFormat decimalFormatE;

    public BasicArithmeticProvider() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("0.#######", symbols);
        decimalFormat.setMaximumFractionDigits(8);

        decimalFormatE = new DecimalFormat("0.#####E0", symbols);

        init();
    }

    @Override
    public Token multiply(Token token1, Token token2) throws CannotParseExpressionException {
        return new Token(format(token1.toDouble() * token2.toDouble()), Token.Type.VALUE);
    }

    @Override
    public List<Token> sanitize(List<Token> tokens) throws CannotParseExpressionException {
        boolean previousIsValue = false;
        List<Token> result = new ArrayList<>();
        for (Token t : tokens) {
            if (!previousIsValue && t.getString().equals("+")) continue;
            if (!previousIsValue && t.getString().equals("-")) {
                result.add(new Token("-1", Token.Type.VALUE));
                result.add(new Token("*", Token.Type.OPERATOR));
                continue;
            }
            previousIsValue = !t.getString().equals("+") && !t.getString().equals("-");

            result.add(t);
        }

        if (result.size() == 1) {
            result.add(new Token("+", Token.Type.OPERATOR));
            result.add(new Token("0", Token.Type.VALUE));
        }
        return result;
    }


    private void init() {

        /*
        BASIC OPERATORS
         */
        addFunction(new BinaryOperator.Builder().name("+").priority(1).computable(args -> {
            if (args.size() != 2) throw new CannotParseExpressionException();
            return new Token(format(args.get(0).toDouble() + args.get(1).toDouble()), Token.Type.VALUE);
        }).build());

        addFunction(new BinaryOperator.Builder().name("-").priority(1).computable(args -> {
            if (args.size() != 2) throw new CannotParseExpressionException();
            return new Token(format(args.get(0).toDouble() - args.get(1).toDouble()), Token.Type.VALUE);
        }).build());

        addFunction(new BinaryOperator.Builder().name("*").priority(2).aliases(Set.of("×")).computable(args -> {
            if (args.size() != 2) throw new CannotParseExpressionException();
            return new Token(format(args.get(0).toDouble() * args.get(1).toDouble()), Token.Type.VALUE);
        }).build());

        addFunction(new BinaryOperator.Builder().name("/").priority(2).aliases(Set.of("÷")).computable(args -> {
            if (args.size() != 2) throw new CannotParseExpressionException();
            return new Token(format(args.get(0).toDouble() / args.get(1).toDouble()), Token.Type.VALUE);
        }).build());


        /*
        ADVANCED OPERATORS
         */
        addFunction(new UnaryOperator.Builder().name("!").priority(100).postfix(true).computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(factorial(args.get(0).toInt())), Token.Type.VALUE);
        }).build());

        addFunction(new BinaryOperator.Builder().name("%").priority(3).computable(args -> {
            if (args.size() != 2) throw new CannotParseExpressionException();
            return new Token(format((args.get(0).toDouble() % args.get(1).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new BinaryOperator.Builder().name("^").aliases(Set.of("**")).priority(4).computable(args -> {
            if (args.size() != 2) throw new CannotParseExpressionException();
            return new Token(format(Math.pow(args.get(0).toDouble(), args.get(1).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new BinaryOperator.Builder().name("//").priority(3).computable(args -> {
            if (args.size() != 2) throw new CannotParseExpressionException();
            return new Token(format((int) (args.get(0).toDouble() / args.get(1).toDouble())), Token.Type.VALUE);
        }).build());


        /*
        TRIGONOMETRY FUNCTIONS
         */
        addFunction(new ArgsFunction.Builder().name("sin").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.sin(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("cos").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.cos(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("tan").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.tan(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("sind").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.sin(Math.toRadians(args.get(0).toDouble()))), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("cosd").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.cos(Math.toRadians(args.get(0).toDouble()))), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("tand").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.tan(Math.toRadians(args.get(0).toDouble()))), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("sinh").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.sinh(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("cosh").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.cosh(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("tanh").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.tanh(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("asin").computable(args -> {
            System.out.println("|" + args.get(0) + "| " + args.size());
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.asin(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("acos").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.acos(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("atan").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.atan(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());


        /*
        OTHER FUNCTIONS
         */
        addFunction(new ArgsFunction.Builder().name("max").computable(args -> {
            if (args.isEmpty()) throw new CannotParseExpressionException();
            double max = args.get(0).toDouble();
            for (Token t : args) {
                double d = t.toDouble();
                if (d > max) max = d;
            }
            return new Token(format(max), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("min").computable(args -> {
            if (args.isEmpty()) throw new CannotParseExpressionException();
            double min = args.get(0).toDouble();
            for (Token t : args) {
                double d = t.toDouble();
                if (d < min) min = d;
            }
            return new Token(format(min), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("rand").computable(args -> {
            //if (args.isEmpty()) throw new CannotParseExpressionException();
            return new Token(format(new Random().nextDouble()), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("ceil").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.ceil(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("floor").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.floor(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("ln").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.log(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("log10").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.log10(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

        addFunction(new ArgsFunction.Builder().name("sqrt").computable(args -> {
            if (args.size() != 1) throw new CannotParseExpressionException();
            return new Token(format(Math.sqrt(args.get(0).toDouble())), Token.Type.VALUE);
        }).build());

    }

    private String format(double d) {
        //System.out.println("f: "+d);
        String res;
        if (Math.abs(d) > 1000000000) res = decimalFormatE.format(d);
        else res = decimalFormat.format(d);
        if (res.equals("-0")) res = "0";
        return res;
    }

    private int factorial(int n) {
        return n == 0 ? 1 : factorial(n - 1) * n;
    }


}
