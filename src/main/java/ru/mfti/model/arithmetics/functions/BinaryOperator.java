package ru.mfti.model.arithmetics.functions;

import ru.mfti.model.Token;

import java.util.List;

public class BinaryOperator extends CalcFunction{


    protected BinaryOperator(Builder builder) {
        super(builder);
    }

    public static class Builder extends CalcFunction.Builder<Builder> {

        @Override
        public Builder me(){
            return this;
        }

        @Override
        public BinaryOperator build(){
            return new BinaryOperator(this);
        }


    }

}
