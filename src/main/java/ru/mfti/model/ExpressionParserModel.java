package ru.mfti.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mfti.model.exceptions.CannotParseExpressionException;
import ru.mfti.model.util.ExpUtil;

import java.util.List;
import java.util.Optional;


@Service
public class ExpressionParserModel {


    private final TokenManager tokenManager;


    @Autowired
    public ExpressionParserModel(TokenManager tokenManager) {
        try {
            repairReducedMultiplication("(1+2)(2+3)");
        } catch (Exception e) {

        }
        this.tokenManager = tokenManager;
    }

    public Optional<String> parseExpression(String expression) {
        try {
            if (!validateBrackets(expression)) expression = repairBrackets(expression);
            //System.out.println(!validateBrackets(expression) + " " + expression);
            expression = removeSpaces(expression);
            expression = repairReducedMultiplication(expression);
            return Optional.of(tokenManager.simplifyToken(new Token(expression, Token.Type.COMPLEX)).getString());
        } catch (CannotParseExpressionException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    private boolean validateBrackets(String expression) throws CannotParseExpressionException {
        Optional<Integer> bracketErrorIndex = ExpUtil.findBracketMismatch(expression);
        return bracketErrorIndex.isEmpty();
    }

    private String repairBrackets(String expression) throws CannotParseExpressionException {
        return ExpUtil.fixBrackets(expression);
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
