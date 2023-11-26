package ru.mfti.model.arithmetics.functions;

import ru.mfti.model.exceptions.CannotAddFunctionException;
import ru.mfti.model.util.ExpUtil;

import java.util.stream.Stream;

public class ArgsFunction extends CalcFunction {


    protected ArgsFunction(Builder builder) {
        super(builder);

        if (Stream.concat(Stream.of(name), getAliases().stream()).flatMap(s -> s.chars().boxed()).anyMatch(c -> ExpUtil.isOperatorTokenCharacter((char) (c.intValue())))) {
            throw new CannotAddFunctionException("Function " + this.name + " has forbidden characters!");
        }
    }


    public static class Builder extends CalcFunction.Builder<ArgsFunction.Builder> {


        @Override
        public Builder me() {
            return this;
        }

        @Override
        public ArgsFunction build() {
            return new ArgsFunction(this);
        }


    }

}
