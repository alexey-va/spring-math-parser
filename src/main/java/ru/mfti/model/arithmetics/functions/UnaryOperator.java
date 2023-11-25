package ru.mfti.model.arithmetics.functions;

import ru.mfti.model.Token;

import java.util.List;

public class UnaryOperator extends CalcFunction {

    boolean postfix = true;

    protected UnaryOperator(Builder builder) {
        super(builder);
        this.postfix = builder.postfix;
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
