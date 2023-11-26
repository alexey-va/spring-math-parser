package ru.mfti.model.arithmetics.functions;

import ru.mfti.model.exceptions.CannotAddFunctionException;
import ru.mfti.model.util.ExpUtil;

import java.util.stream.Stream;

public class BinaryOperator extends CalcFunction {


    protected BinaryOperator(Builder builder) {
        super(builder);
        if (!Stream.concat(Stream.of(name), getAliases().stream()).flatMap(s -> s.chars().boxed()).allMatch(c -> ExpUtil.isOperatorTokenCharacter((char) (c.intValue())))) {
            throw new CannotAddFunctionException("Operator " + this.name + " has forbidden characters!");
        }
    }

    public static class Builder extends CalcFunction.Builder<Builder> {

        @Override
        public Builder me() {
            return this;
        }

        @Override
        public BinaryOperator build() {
            return new BinaryOperator(this);
        }


    }

}
