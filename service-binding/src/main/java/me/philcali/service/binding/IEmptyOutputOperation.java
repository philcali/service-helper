package me.philcali.service.binding;

public interface IEmptyOutputOperation<I> extends IOperation<I, Void> {
    void accept(I input);

    @Override
    default Void apply(final I input) {
        accept(input);
        return null;
    }
}
