package me.philcali.service.binding;

import java.util.function.Function;

@FunctionalInterface
public interface IOperation<I, O> extends Function<I, O> {
    @Override
    O apply(I input);
}
