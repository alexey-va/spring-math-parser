package ru.mfti.model.util;

import ru.mfti.model.exceptions.CannotParseExpressionException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Stack;

public class ExpUtil {


    public static String trimBrackets(String expression) {
        while (!expression.isEmpty() && isOpeningBracket(expression.charAt(0))) {
            int bracketIndex = findConnectedBracket(expression, 0);
            if (bracketIndex == expression.length() - 1) expression = expression.substring(1, expression.length() - 1);
            else break;
        }
        return expression;
    }

    public static int findConnectedBracket(String expression, int index) {
        if (index >= expression.length()) return -1;

        char c = expression.charAt(index);
        if (!isBracket(c)) return -1;
        boolean originIsOpeningBracket = isOpeningBracket(c);

        Stack<Character> brackets = new Stack<>();
        while (index >= 0 && index < expression.length()) {
            c = expression.charAt(index);
            if (isOpeningBracket(c)) {
                if (originIsOpeningBracket) brackets.push(c);
                else brackets.pop();
            } else if (isClosingBracket(c)) {
                if (originIsOpeningBracket) brackets.pop();
                else brackets.push(c);
            }

            if (brackets.empty()) return index;

            if (originIsOpeningBracket) index++;
            else index--;
        }
        return -1;
    }

    public static Optional<String> getEnvelopingFunctionName(String expression) {
        StringBuilder functionNameBuilder = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (isValueCharacter(c)) functionNameBuilder.append(c);
            else if (isOpeningBracket(c)) {
                int connectedBracketIndex = findConnectedBracket(expression, i);
                if (connectedBracketIndex == expression.length() - 1 && !functionNameBuilder.isEmpty())
                    return Optional.of(functionNameBuilder.toString());
                else return Optional.empty();
            } else return Optional.empty();
        }
        return Optional.empty();
    }

    public static String stripEnvelopingFunction(String expression) {
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (!(isValueCharacter(c) || isOpeningBracket(c))) return expression;
            if (isOpeningBracket(c)) {
                int connectedBracketIndex = findConnectedBracket(expression, i);
                if (connectedBracketIndex == expression.length() - 1)
                    return expression.substring(i + 1, expression.length() - 1);
                return expression;
            }
        }
        return expression;
    }


    public static char getOppositeBracket(char c) {
        return switch (c) {
            case '{' -> '}';
            case '[' -> ']';
            case '(' -> ')';
            case '}' -> '{';
            case ']' -> '[';
            case ')' -> '(';
            default -> (char) 0;
        };
    }


    public static Optional<Integer> findBracketMismatch(String expression) {
        Deque<Character> deque = new ArrayDeque<>();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (Character.isSpaceChar(c)) continue;
            if (isOpeningBracket(c)) deque.push(c);
            else if (isClosingBracket(c)) {
                if (deque.isEmpty() || deque.pop() != getOppositeBracket(c)) return Optional.of(i);
            }
        }
        return deque.isEmpty() ? Optional.empty() : Optional.of(0);
    }

    public static Optional<String> getLongestNumberSubstring(String s, int index) {
        StringBuilder builder = new StringBuilder();
        if (s.isEmpty()) return Optional.empty();
        if (!isNumberCharacter(s.charAt(index))) return Optional.empty();

        builder.append(s.charAt(index));
        for (int i = 1; i < s.length() - index; i++) {
            char c = s.charAt(i);
            if (!isNumberCharacter(c)) break;
            builder.append(c);
        }

        String result = builder.toString();
        if (isNumber(result)) return Optional.of(result);
        return Optional.empty();

    }


    public static boolean isNumber(String s) {
        s = s.trim();
        boolean dotSeen = false;
        boolean hasDigists = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) hasDigists = true;
            if (c == '-' || c == '+') {
                if (i == 0) continue;
                return false;
            }
            if (c == '.') {
                if (dotSeen) return false;
                dotSeen = true;
            }
            if (!isValueCharacter(c)) return false;
        }
        return hasDigists;
    }

    public static boolean isNumberCharacter(char c) {
        return Character.isDigit(c) || c == '.' || c == '-' || c == '+';
    }

    public static boolean isOperatorTokenCharacter(char c) {
        return !(Character.isLetter(c) || Character.isDigit(c) || isBracket(c) || c == ',' || c == '.');
    }

    public static boolean isValueCharacter(char c) {
        return (Character.isLetter(c) || Character.isDigit(c) || c == ',' || c == '.');
    }

    public static boolean isBracket(char c) {
        return c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}';
    }

    public static boolean isOpeningBracket(char c) {
        return c == '(' || c == '[' || c == '{';
    }

    public static boolean isClosingBracket(char c) {
        return c == ')' || c == ']' || c == '}';
    }


    public static String fixBrackets(String expression) throws CannotParseExpressionException {
        Deque<Character> deque = new ArrayDeque<>();
        Deque<Character> toAdd = new ArrayDeque<>();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (Character.isSpaceChar(c)) continue;
            if (isOpeningBracket(c)) deque.push(c);
            else if (isClosingBracket(c)) {
                // not enough opening brackets
                if (deque.isEmpty()) toAdd.push(getOppositeBracket(c));
                else if(deque.pop() != getOppositeBracket(c)){
                    throw new CannotParseExpressionException();
                }
            }
        }
        System.out.println(deque+" "+toAdd);
        StringBuilder expressionBuilder = new StringBuilder(expression);
        while (!toAdd.isEmpty()) expressionBuilder.insert(0, toAdd.removeLast());
        while (!deque.isEmpty()) expressionBuilder.append(getOppositeBracket(deque.pop()));

        expression = expressionBuilder.toString();
        return expression;
    }
}
