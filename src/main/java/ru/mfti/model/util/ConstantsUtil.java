package ru.mfti.model.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConstantsUtil {

    private static final Map<String, Double> CONSTANT_MAP = new HashMap<>(){{
       put("pi", Math.PI);
       put("e", Math.E);
       put("π", Math.PI);
       put("τ", 2*Math.PI);
    }};


    public static Optional<Double> getConstant(String name){
        Double val = CONSTANT_MAP.get(name);
        return val == null ? Optional.empty() : Optional.of(val);
    }

    public static Optional<Double> parseConstants(String stringOfConsts) {

        double result = 1;
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<stringOfConsts.length();i++){
            builder.append(stringOfConsts.charAt(i));
            Optional<Double> opt =  getConstant(builder.toString());
            if(opt.isPresent()){
                result*= opt.get();
                builder = new StringBuilder();
            }
        }

        if(builder.isEmpty()) return Optional.of(result);
        else return Optional.empty();
    }
}
