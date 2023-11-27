package ru.mfti.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mfti.model.exceptions.CannotParseExpressionException;
import ru.mfti.model.util.ExpUtil;

import java.util.Optional;


@Service
public class ExpressionParserModel {


    private final TokenManager tokenManager;


    @Autowired
    public ExpressionParserModel(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public Optional<String> parseExpression(String expression) {
        try {
            // Fix possible mistakes in expression such as:
            // sin(pi/2 - not closed brackets
            // 1 + 2 + 3 - spaces
            // 10(1+2) or (1+2)(2+3) - reduced * sign before brackets
            expression = repairBrackets(expression);
            expression = removeSpaces(expression);
            expression = repairReducedMultiplication(expression);

            Token wrappedExpression = new Token(expression, Token.Type.COMPLEX);
            //System.out.println(wrappedExpression);
            return Optional.of(tokenManager.simplifyToken(wrappedExpression).getString());
        } catch (CannotParseExpressionException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    private boolean validateBrackets(String expression) {
        Optional<Integer> bracketErrorIndex = ExpUtil.findBracketMismatch(expression);
        return bracketErrorIndex.isEmpty();
    }

    private String repairBrackets(String expression) throws CannotParseExpressionException {
        if (!validateBrackets(expression)) return ExpUtil.fixBrackets(expression);
        return expression;
    }

    private String repairReducedMultiplication(String expression) {
        StringBuilder builder = new StringBuilder();
        for (char c : expression.toCharArray()) {
            if (builder.isEmpty()) {
                builder.append(c);
                continue;
            }

            if (ExpUtil.isClosingBracket(builder.charAt(builder.length() - 1)) && (ExpUtil.isValueCharacter(c) || ExpUtil.isOpeningBracket(c)))
                builder.append("*");
            builder.append(c);
        }
        return builder.toString();
    }

    private String removeSpaces(String expression) {
        return expression.replaceAll("\s", "");
    }


}
