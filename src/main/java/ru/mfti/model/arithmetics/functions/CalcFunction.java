package ru.mfti.model.arithmetics.functions;

import ru.mfti.model.Token;
import ru.mfti.model.exceptions.CannotParseExpressionException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class CalcFunction implements Computable {

    String name;
    Set<String> aliases;
    Computable computable;
    int priority;

    protected CalcFunction(final Builder<?> builder) {
        this.name = builder.name;
        this.aliases = builder.aliases;
        this.computable = builder.computable;
        this.priority = builder.priority;


    }

    public Token compute(List<Token> args) throws CannotParseExpressionException {
        return computable.compute(args);
    }

    public String getName() {
        return name;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract static class Builder<T extends Builder<T>> {
        private String name;
        private Set<String> aliases = new HashSet<>();
        private Computable computable;
        int priority = 0;

        public T name(String name) {
            this.name = name;
            return me();
        }

        public T aliases(Set<String> set) {
            this.aliases = set;
            return me();
        }

        public T computable(Computable computable) {
            this.computable = computable;
            return me();
        }

        public T priority(int priority) {
            this.priority = priority;
            return me();
        }

        public abstract T me();

        public abstract CalcFunction build();
    }

}
