package ru.mfti.model.arithmetics.functions;

import ru.mfti.model.Token;

import java.util.List;

public class ArgsFunction extends CalcFunction{



    protected ArgsFunction(Builder builder){
        super(builder);
    }


    public static class Builder extends CalcFunction.Builder<ArgsFunction.Builder> {


        @Override
        public Builder me(){
            return this;
        }

        @Override
        public ArgsFunction build(){
            return new ArgsFunction(this);
        }


    }

}
