package ru.mfti.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mfti.model.arithmetics.ArithmeticProvider;
import ru.mfti.model.exceptions.CannotParseExpressionException;

import java.util.*;

import static ru.mfti.model.util.ExpUtil.*;

@Component
public class TokenManager {

    private final ArithmeticProvider arithmeticProvider;

    @Autowired
    public TokenManager(ArithmeticProvider arithmeticProvider) {
        this.arithmeticProvider = arithmeticProvider;
    }


    public List<Token> tokenize(Token expression) throws CannotParseExpressionException {
        List<Token> result = new ArrayList<>();
        Deque<Character> bracketStack = new ArrayDeque<>();
        StringBuilder valueTokenBuilder = new StringBuilder();
        boolean complex = false;

        for (int i = 0; i < expression.getString().length(); i++) {
            char c = expression.getString().charAt(i);
            //System.out.println("Char " + c + " op: " + ExpUtil.isOperatorTokenCharacter(c));

            boolean addToToken = !bracketStack.isEmpty() || isValueCharacter(c) || (isBracket(c));

            // add to value string builder
            if (addToToken) {
                valueTokenBuilder.append(c);
                //System.out.println("appending: "+c);
                if (!bracketStack.isEmpty()) complex = true;
            }

            // flush token if encounter operator or closing bracket or last character
            if ((!addToToken && !valueTokenBuilder.isEmpty()) || i == expression.getString().length() - 1) {
                result.add(new Token(valueTokenBuilder.toString(), complex ? Token.Type.COMPLEX : Token.Type.VALUE));
                valueTokenBuilder = new StringBuilder();
            }

            // do some bracket counting
            if (isOpeningBracket(c)) {
                bracketStack.push(c);
            } else if (isClosingBracket(c)) {
                if (bracketStack.isEmpty() || bracketStack.pop() != getOppositeBracket(c))
                    throw new CannotParseExpressionException("Bracket mismatch at index " + i);
            }
            // get operator full substring (it can be multiple chars long like //)
            else if (!addToToken && isOperatorTokenCharacter(c)) {
                Optional<String> fullToken = getOperatorToken(expression.getString(), i);
                //System.out.println("Full token: " + fullToken + " from " + i + " " + c);
                if (fullToken.isEmpty()) throw new CannotParseExpressionException("Operator " + c + " is not found!");
                result.add(new Token(fullToken.get(), Token.Type.OPERATOR));
                i += fullToken.get().length() - 1;
            }
        }
        return arithmeticProvider.sanitize(result);
    }


    public Optional<String> getOperatorToken(String exp, int tokenStartIndex) {
        StringBuilder operatorString = new StringBuilder();
        for (int i = tokenStartIndex; i < exp.length(); i++) {
            char c = exp.charAt(i);
            if (!isOperatorTokenCharacter(c)) break;
            operatorString.append(c);
        }

        operatorString.reverse();

        while (!arithmeticProvider.isOperatorName(operatorString.toString())
                && !operatorString.isEmpty()) operatorString.deleteCharAt(0);
        return !operatorString.isEmpty() ? Optional.of(operatorString.toString()) : Optional.empty();
    }

    public Token simplifyToken(Token token) throws CannotParseExpressionException {
        //System.out.println(token);
        if (token.getType() != Token.Type.COMPLEX) return token;

        // If token is a function (e.g. sin(...))
        if (token.getEnvelopingFunction() != null) {
            List<Token> simplifiedArguments = new ArrayList<>();
            for (Token argToken : token.getArgumentTokens()) simplifiedArguments.add(simplifyToken(argToken));
            return arithmeticProvider.applyFunction(token, simplifiedArguments);
        }

        List<Token> subTokens = tokenize(token);
        List<Token> simplifiedTokens = new ArrayList<>();
        for (Token t : subTokens) {
            Token simplified = t.type == Token.Type.COMPLEX ? simplifyToken(t) : t;
            //System.out.println(simplified);
            simplifiedTokens.add(simplified);
        }
        Token result = shuntingYard(simplifiedTokens);
        if (result.getType() != Token.Type.VALUE)
            throw new CannotParseExpressionException("Could not parse token: " + token);
        //System.out.println(result+" "+result.getType());
        return result;
    }


    private Token shuntingYard(List<Token> simplifiedTokenList) throws CannotParseExpressionException {
        //System.out.println("sy: "+simplifiedTokenList);
        Deque<Token> numbers = new ArrayDeque<>();
        Deque<Token> operators = new ArrayDeque<>();

        for (Token currentToken : simplifiedTokenList) {
            System.out.println(currentToken+" "+currentToken.getType());
            if (currentToken.type == Token.Type.VALUE) {
                numbers.push(currentToken);
                continue;
            }

            if (!operators.isEmpty()) {
                Token previousOperator = operators.peek();

                int previousOperatorPriority = arithmeticProvider.getOperatorPriority(previousOperator);
                int currentOperatorPriority = arithmeticProvider.getOperatorPriority(currentToken);

                // Do operation with previous operator cuz current is lower priority
                if (currentOperatorPriority < previousOperatorPriority) {
                    if(arithmeticProvider.isBinaryOperator(previousOperator)) {
                        Token num2 = numbers.pop();
                        Token num1 = numbers.pop();
                        Token operator = operators.pop();
                        Token result = arithmeticProvider.applyFunction(operator, List.of(num1, num2));
                        numbers.push(result);
                    } else if(arithmeticProvider.isPostfixOperator(previousOperator) ||
                            arithmeticProvider.isPrefixOperator(previousOperator)){
                        Token num = numbers.pop();
                        Token operator = operators.pop();
                        Token result = arithmeticProvider.applyFunction(operator, List.of(num));
                        numbers.push(result);
                    }
                }
            }
            operators.push(currentToken);
        }

        //System.out.println(numbers+" "+operators);
        while (numbers.size() > 1 || !operators.isEmpty()) {

            Token operator = operators.pollFirst();
            if(arithmeticProvider.isBinaryOperator(operator)) {
                Token num2 = numbers.pollFirst();
                Token num1 = numbers.pollFirst();
                Token result = arithmeticProvider.applyFunction(operator, List.of(num1, num2));
                //System.out.println("res:"+result+" "+num1+" "+num2);
                numbers.addFirst(result);
                //System.out.println("numbers: "+numbers);
            } else if(arithmeticProvider.isPostfixOperator(operator) ||
                    arithmeticProvider.isPrefixOperator(operator)){
                Token num = numbers.pollFirst();
                Token result = arithmeticProvider.applyFunction(operator, List.of(num));
                //System.out.println("res:"+result);
                numbers.addFirst(result);
            }
        }

        return numbers.pop();
    }


}
