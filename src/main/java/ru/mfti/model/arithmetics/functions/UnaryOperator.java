package ru.mfti.model.arithmetics.functions;

import ru.mfti.model.exceptions.CannotAddFunctionException;
import ru.mfti.model.util.ExpUtil;

import java.util.stream.Stream;

public class UnaryOperator extends CalcFunction {

    boolean postfix;

    protected UnaryOperator(Builder builder) {
        super(builder);
        this.postfix = builder.postfix;
        if (!Stream.concat(Stream.of(name), getAliases().stream()).flatMap(s -> s.chars().boxed()).allMatch(c -> ExpUtil.isOperatorTokenCharacter((char) (c.intValue())))) {
            throw new CannotAddFunctionException("Operator " + this.name + " has forbidden characters!");
        }
    }

    public boolean isPostfix() {
        return postfix;
    }

    public static class Builder extends CalcFunction.Builder<Builder> {
        private boolean postfix = true;

        public Builder postfix(boolean postfix) {
            this.postfix = postfix;
            return this;
        }

        @Override
        public Builder me() {
            return this;
        }

        @Override
        public UnaryOperator build() {
            return new UnaryOperator(this);
        }


    }

}
