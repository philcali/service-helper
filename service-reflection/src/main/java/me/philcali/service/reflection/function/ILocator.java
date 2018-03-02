package me.philcali.service.reflection.function;

public interface ILocator<T> {
    boolean isApplicable(T thing);
}
