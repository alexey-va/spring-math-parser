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


    /**
     * @param expression expression, represented as a Token wrapper
     * @return list of subtokens, which may contain value tokens, operator tokens
     * and complex tokens
     * @throws CannotParseExpressionException
     */
    public List<Token> tokenize(Token expression) throws CannotParseExpressionException {
        List<Token> result = new ArrayList<>();
        Deque<Character> bracketStack = new ArrayDeque<>();
        StringBuilder valueTokenBuilder = new StringBuilder();
        boolean complex = false;

        for (int i = 0; i < expression.getString().length(); i++) {
            char c = expression.getString().charAt(i);

            boolean addToToken = !bracketStack.isEmpty() || isValueCharacter(c) || (isBracket(c));

            // add to value string builder & if inside bracket - mark as a complex token
            if (addToToken) {
                valueTokenBuilder.append(c);
                if (!bracketStack.isEmpty()) complex = true;
            }

            // flush token if encounter operator or closing bracket or last character
            if ((!addToToken && !valueTokenBuilder.isEmpty()) || i == expression.getString().length() - 1) {
                result.add(new Token(valueTokenBuilder.toString(), complex ? Token.Type.COMPLEX : Token.Type.VALUE));
                valueTokenBuilder = new StringBuilder();
            }

            // Do bracket counting
            if (isOpeningBracket(c)) bracketStack.push(c);
            else if (isClosingBracket(c) && (bracketStack.isEmpty() || bracketStack.pop() != getOppositeBracket(c)))
                throw new CannotParseExpressionException("Incorrent amount of brackets on tokenize stage. This should not happen!");

                // get operator full substring (it can be multiple chars long like //)
            else if (!addToToken && isOperatorTokenCharacter(c)) {
                Optional<String> fullToken = getOperatorToken(expression.getString(), i);
                if (fullToken.isEmpty()) throw new CannotParseExpressionException("Operator " + c + " is not found!");

                Token operator = new Token(fullToken.get(), Token.Type.OPERATOR);

                // Check for missing value of postfix operators like factorial "!"
                if (arithmeticProvider.isPostfixOperator(operator) && (result.isEmpty() || result.get(result.size() - 1).type == Token.Type.OPERATOR))
                    throw new CannotParseExpressionException("No value for unary postfix operator!");

                result.add(new Token(fullToken.get(), Token.Type.OPERATOR));
                i += fullToken.get().length() - 1;
            }
        }
        return arithmeticProvider.sanitize(result);
    }


    /**
     * @param expression
     * @param operatorStart index of the first char of operator
     * @return Optinal which contains operator token if it matches
     * any from ArithmeticProvider
     */
    public Optional<String> getOperatorToken(String expression, int operatorStart) {
        StringBuilder operatorString = new StringBuilder();
        for (int i = operatorStart; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (!isOperatorTokenCharacter(c)) break;
            operatorString.append(c);
        }

        operatorString.reverse();

        while (!arithmeticProvider.isOperatorName(operatorString.toString()) && !operatorString.isEmpty())
            operatorString.deleteCharAt(0);

        return !operatorString.isEmpty() ? Optional.of(operatorString.toString()) : Optional.empty();
    }


    /**
     * Recursive method which turns complex expressions into a simple value
     *
     * @param token wrapped math expression
     * @return parsed value wrapped in Token
     * @throws CannotParseExpressionException when any mistake in expression is found
     */
    public Token simplifyToken(Token token) throws CannotParseExpressionException {
        if (token.getType() != Token.Type.COMPLEX) return token;

        // If token is a function (e.g. sin(...))
        if (token.getEnvelopingFunction() != null) {
            List<Token> simplifiedArguments = new ArrayList<>();
            for (Token argToken : token.getArgumentTokens()) simplifiedArguments.add(simplifyToken(argToken));
            return arithmeticProvider.applyFunction(token, simplifiedArguments);
        }

        List<Token> subTokens = tokenize(token);
        List<Token> simplifiedTokens = new ArrayList<>();
        for (Token t : subTokens) simplifiedTokens.add(t.type == Token.Type.COMPLEX ? simplifyToken(t) : t);

        Token result = applyOperatorsInOrder(simplifiedTokens);

        if (result.getType() != Token.Type.VALUE)
            throw new CannotParseExpressionException("Result is not a number! Wrong input expression");

        return result;
    }


    /**
     * @param simplifiedTokenList list of numbers and operators wrapped in Token
     * @return resulting value wrapped in Token
     * @throws CannotParseExpressionException
     */
    private Token applyOperatorsInOrder(List<Token> simplifiedTokenList) throws CannotParseExpressionException {
        List<Integer> operatorPriorities = simplifiedTokenList.stream()
                .filter(t -> arithmeticProvider.isOperatorName(t.getString()))
                .map(arithmeticProvider::getOperatorPriority)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        for (int priority : operatorPriorities) {
            int i = 0;
            while (i < simplifiedTokenList.size()) {
                Token t = simplifiedTokenList.get(i);
                if (t.type != Token.Type.OPERATOR || arithmeticProvider.getOperatorPriority(t) != priority)
                    i++;
                else if (arithmeticProvider.isBinaryOperator(t)) {
                    Token num1 = simplifiedTokenList.remove(i - 1);
                    Token num2 = simplifiedTokenList.remove(i);
                    simplifiedTokenList.remove(t);
                    simplifiedTokenList.add(i - 1, arithmeticProvider.applyFunction(t, List.of(num1, num2)));
                } else if (arithmeticProvider.isPrefixOperator(t)) {
                    Token num = simplifiedTokenList.remove(i + 1);
                    simplifiedTokenList.remove(t);
                    simplifiedTokenList.add(i, arithmeticProvider.applyFunction(t, List.of(num)));
                    i++;
                } else if (arithmeticProvider.isPostfixOperator(t)) {
                    Token num = simplifiedTokenList.remove(i - 1);
                    simplifiedTokenList.remove(t);
                    simplifiedTokenList.add(i - 1, arithmeticProvider.applyFunction(t, List.of(num)));
                }
            }
        }

        return simplifiedTokenList.get(0);
    }


}
